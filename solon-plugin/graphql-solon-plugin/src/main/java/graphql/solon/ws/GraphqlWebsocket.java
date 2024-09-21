package graphql.solon.ws;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.solon.support.WebGraphQlHandler;
import graphql.solon.support.WebGraphQlHandlerGetter;
import graphql.solon.support.WebGraphQlResponse;
import graphql.solon.support.WebSocketGraphQlInterceptor;
import graphql.solon.util.Assert;
import graphql.solon.ws.support.GraphQlWebSocketMessage;
import graphql.solon.ws.support.WebSocketGraphQlRequest;
import graphql.solon.ws.support.WebSocketSessionInfo;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.collections.CollectionUtils;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Inject;
import org.noear.solon.lang.Nullable;
import org.noear.solon.net.annotation.ServerEndpoint;
import org.noear.solon.net.websocket.SubProtocolCapable;
import org.noear.solon.net.websocket.WebSocket;
import org.noear.solon.net.websocket.listener.SimpleWebSocketListener;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * @author fuzi1996
 * @since 2.3
 */
@ServerEndpoint("/graphql")
public class GraphqlWebsocket extends SimpleWebSocketListener implements SubProtocolCapable {

    private static final Logger log = LoggerFactory.getLogger(GraphqlWebsocket.class);

    private final Map<String, SessionState> sessionInfoMap = new ConcurrentHashMap<>();

    @Inject
    private WebGraphQlHandlerGetter getter;

    @Override
    public String getSubProtocols(Collection<String> requestProtocols) {
        return "graphql-transport-ws";
    }

    @Override
    public void onOpen(WebSocket socket) {
        SessionState sessionState = new SessionState(socket.id(),
            new WebMvcSessionInfo(socket));
        this.sessionInfoMap.put(socket.id(), sessionState);
    }

    @Override
    public void onClose(WebSocket socket) {
        SessionState state = this.sessionInfoMap.remove(socket.id());
        if (Objects.nonNull(state)) {
            state.dispose();

            Map<String, Object> connectionInitPayload = state.getConnectionInitPayload();
            if (connectionInitPayload != null) {
                WebGraphQlHandler graphQlHandler = this.getter.getGraphQlHandler();
                WebSocketGraphQlInterceptor webSocketInterceptor = graphQlHandler
                    .getWebSocketInterceptor();
                webSocketInterceptor.handleConnectionClosed(
                    state.getSessionInfo(), connectionInitPayload);
            }
        }
    }

    @Override
    public void onError(WebSocket socket, Throwable error) {
        SessionState state = this.sessionInfoMap.remove(socket.id());
        if (Objects.nonNull(state)) {
            state.dispose();
        }

    }

    @Override
    public void onMessage(WebSocket socket, String text) {
        GraphQlWebSocketMessage graphQlWebSocketMessage = ONode.loadStr(text)
            .toObject(GraphQlWebSocketMessage.class);
        String id = graphQlWebSocketMessage.getId();
        SessionState state = getSessionInfo(socket);

        switch (graphQlWebSocketMessage.resolvedType()) {
            case CONNECTION_INIT:
                GraphQlWebSocketMessage respMessage = GraphQlWebSocketMessage
                    .connectionAck(Collections.emptyMap());
                String respStr = ONode.stringify(respMessage);
                socket.send(respStr);
                break;
            case SUBSCRIBE:
                if (Objects.isNull(id)) {
                    socket.close();
                }
                String url = socket.url();
                URI uri = URI.create(url);
                Map<String, Object> payload = (Map<String, Object>) graphQlWebSocketMessage
                    .getPayload();
                WebSocketGraphQlRequest request = new WebSocketGraphQlRequest(uri,
                    Collections.emptyMap(), payload, id,
                    Locale.getDefault(), state.sessionInfo);
                this.getter.getGraphQlHandler().handleRequest(request)
                    .flatMapMany(resp -> this.handleResponse(socket, id, resp))
                    .subscribe(new SendMessageSubscriber(id, socket, state));
                break;
            case COMPLETE:
                if (Objects.nonNull(id)) {
                    Subscription subscription = state.getSubscriptions().remove(id);
                    if (Objects.nonNull(subscription)) {
                        subscription.cancel();
                    }
                    WebGraphQlHandler graphQlHandler = this.getter.getGraphQlHandler();
                    WebSocketGraphQlInterceptor webSocketInterceptor = graphQlHandler
                        .getWebSocketInterceptor();
                    webSocketInterceptor.handleCancelledSubscription(
                        state.getSessionInfo(), id).block(Duration.ofSeconds(10));
                }
                break;
            case PING:
                socket.send(ONode.stringify(GraphQlWebSocketMessage
                    .pong(null)));
                break;
            default:
                socket.close();
        }
    }

    private static class SendMessageSubscriber extends BaseSubscriber<String> {

        private final String subscriptionId;

        private final WebSocket session;

        private final SessionState sessionState;

        SendMessageSubscriber(String subscriptionId, WebSocket session, SessionState sessionState) {
            this.subscriptionId = subscriptionId;
            this.session = session;
            this.sessionState = sessionState;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            subscription.request(1);
        }

        @Override
        protected void hookOnNext(String nextMessage) {
            this.session.send(nextMessage);
            request(1);
        }

        @Override
        public void hookOnError(Throwable ex) {
            this.tryCloseWithError(this.session, ex, log);
        }

        @Override
        public void hookOnComplete() {
            this.sessionState.getSubscriptions().remove(this.subscriptionId);
        }

        public void tryCloseWithError(WebSocket session, Throwable exception, Logger logger) {
            if (logger.isErrorEnabled()) {
                logger.error("Closing session due to exception for " + session, exception);
            }
            if (session.isValid()) {
                try {
                    session.close();
                } catch (Throwable ex) {
                    // ignore
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private Flux<String> handleResponse(WebSocket session, String id, WebGraphQlResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Execution result ready"
                + (!CollectionUtils.isEmpty(response.getErrors()) ? " with errors: " + response
                .getErrors() : "")
                + ".");
        }
        Flux<Map<String, Object>> responseFlux;
        if (response.getData() instanceof Publisher) {
            // Subscription
            responseFlux = Flux.from((Publisher<ExecutionResult>) response.getData())
                .map(ExecutionResult::toSpecification)
                .doOnSubscribe((subscription) -> {
                    Subscription prev = getSessionInfo(session).getSubscriptions()
                        .putIfAbsent(id, subscription);
                    if (prev != null) {
                        throw new SubscriptionExistsException();
                    }
                });
        } else {
            // Single response (query or mutation) that may contain errors
            responseFlux = Flux.just(response.toMap());
        }

        return responseFlux
            .map(responseMap -> ONode.stringify(GraphQlWebSocketMessage.next(id, responseMap)))
            .concatWith(
                Mono.fromCallable(() -> ONode.stringify(GraphQlWebSocketMessage.complete(id))))
            .onErrorResume((ex) -> {
                if (ex instanceof SubscriptionExistsException) {
                    session.close();
                    return Flux.empty();
                }
                String message = ex.getMessage();
                GraphQLError error = GraphqlErrorBuilder.newError().message(message).build();
                return Mono.just(ONode.stringify(GraphQlWebSocketMessage.error(id, error)));
            });
    }

    private SessionState getSessionInfo(WebSocket session) {
        SessionState info = this.sessionInfoMap.get(session.id());
        Assert.notNull(info, "No SessionInfo for " + session);
        return info;
    }

    private static class WebMvcSessionInfo implements WebSocketSessionInfo {

        private final WebSocket session;

        private WebMvcSessionInfo(WebSocket session) {
            this.session = session;
        }

        @Override
        public String getId() {
            return this.session.id();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return this.session.attrMap();
        }

        @Override
        public URI getUri() {
            return URI.create(this.session.url());
        }

        @Override
        public InetSocketAddress getRemoteAddress() throws IOException {
            return this.session.remoteAddress();
        }
    }

    private static class SessionState {

        private final WebSocketSessionInfo sessionInfo;

        private final AtomicReference<Map<String, Object>> connectionInitPayloadRef = new AtomicReference<>();

        private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();

        private final Scheduler scheduler;

        SessionState(String graphQlSessionId, WebSocketSessionInfo sessionInfo) {
            this.sessionInfo = sessionInfo;
            this.scheduler = Schedulers.newSingle("GraphQL-WsSession-" + graphQlSessionId);
        }

        public WebSocketSessionInfo getSessionInfo() {
            return this.sessionInfo;
        }

        @Nullable
        Map<String, Object> getConnectionInitPayload() {
            return this.connectionInitPayloadRef.get();
        }

        boolean setConnectionInitPayload(Map<String, Object> payload) {
            return this.connectionInitPayloadRef.compareAndSet(null, payload);
        }


        Map<String, Subscription> getSubscriptions() {
            return this.subscriptions;
        }

        void dispose() {
            for (Map.Entry<String, Subscription> entry : this.subscriptions.entrySet()) {
                try {
                    entry.getValue().cancel();
                } catch (Throwable ex) {
                    // Ignore and keep on
                }
            }
            this.subscriptions.clear();
            this.scheduler.dispose();
        }

        Scheduler getScheduler() {
            return this.scheduler;
        }

    }

    class SubscriptionExistsException extends RuntimeException {

    }
}
