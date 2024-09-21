package features.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import demo.App;
import demo.product.dto.ProductPriceHistoryDTO;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;
import org.noear.snack.ONode;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonTest;

/**
 * @author fuzi1996
 * @since 2.3
 */
@SolonTest(value = App.class)
public class SubscriptionTest extends HttpTester {

    /**
     * 订阅测试：未完成！会失败
     */
    @Test
    public void testSubscription() throws IOException, InterruptedException, URISyntaxException {
        ProductPriceHistoryDTO product = new ProductPriceHistoryDTO(123L, new Date(), 999);

        ONode param = new ONode();
        ONode variables = new ONode();
        variables.set("productId", product.getId());
        param.set("query",
            ResourceUtil.getResourceAsString("/query/subscriptionProduct.gqls"));
        param.set("variables", variables);
        param.set("operationName", "testSubscription");

        // socket
        WsClient client = new WsClient("ws://127.0.0.1:8081/graphql");

        client.initConnection();

        //发送测试
        client.subscribe(param);

        //休息会儿
        for (int i = 0; i < 3; i++) {
            client.ping();
            Thread.sleep(1000);
            System.out.println("====");
        }

        client.complete();

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            System.out.println("====");
        }

        client.check();
    }

    static class WsClient extends WebSocketClient {

        private String id;

        private boolean isConnected = false;

        private boolean isCompleted = false;

        private final List<Exception> exceptions = new LinkedList<>();

        public WsClient(String serverUri) throws URISyntaxException {
            super(new URI(serverUri));
            this.id = UUID.randomUUID().toString();
        }

        public void check() {
            assertThat(this.exceptions, emptyIterable());
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {

        }

        @Override
        public void onMessage(String message) {
            System.out.printf("--------- Client onMessage --------- %s ---------%n", message);
            MesResult result = ONode.deserialize(message, MesResult.class);
            String type = result.getType();
            if (Objects.equals("connection_ack", type)) {
                this.isConnected = true;
            } else if (Objects.equals("next", type)) {
                String mesId = result.getId();
                if (Objects.equals(this.id, mesId)) {
                    if (!this.isCompleted) {
                        ONode payload = result.getPayload();
                        try {
                            this.checkPaload(payload);
                        } catch (Exception e) {
                            this.exceptions.add(e);
                        }
                    } else {
                        this.exceptions.add(new IllegalArgumentException("订阅关闭不应该接收数据"));
                    }
                }
            }
        }

        private void checkPaload(ONode payload) {
            ONode data = payload.get("data");
            assertThat(data, notNullValue());

            ONode notifyProductPriceChange = data.get("notifyProductPriceChange");
            assertThat(notifyProductPriceChange, notNullValue());

            ONode price = notifyProductPriceChange.get("price");
            Object priceValue = price.toData();
            assertThat(priceValue, instanceOf(Number.class));
        }

        @Override
        public void onClose(int i, String s, boolean b) {

        }

        @Override
        public void onError(Exception e) {
            this.exceptions.add(e);
        }

        public void initConnection() throws InterruptedException {
            if (!this.isConnected) {
                this.connectBlocking();
                this.send("{\"type\":\"connection_init\",\"payload\":{}}");
            }
        }

        public void subscribe(ONode payload) {
            MesResult result = new MesResult();
            result.setId(this.id);
            result.setType("subscribe");
            result.setPayload(payload);
            this.send(ONode.serialize(result));
        }

        public void complete() {
            this.send("{id: \"" + this.id + "\", type: \"complete\"}");
            this.isCompleted = true;
        }

        public void ping() {
            this.send("{id: \"" + this.id + "\", type: \"ping\"}");
        }

        public static class MesResult {

            private String id;
            private ONode payload;
            private String type;

            public MesResult() {
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public ONode getPayload() {
                return payload;
            }

            public void setPayload(ONode payload) {
                this.payload = payload;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }

    }

}
