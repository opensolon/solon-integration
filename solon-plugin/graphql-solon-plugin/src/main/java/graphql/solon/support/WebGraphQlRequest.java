/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package graphql.solon.support;

import graphql.solon.util.Assert;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.lang.Nullable;


/**
 * {@link org.springframework.graphql.GraphQlRequest} implementation for server
 * handling over HTTP or WebSocket. Provides access to the URL and headers of
 * the underlying request. For WebSocket, these are the URL and headers of the
 * HTTP handshake request.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
public class WebGraphQlRequest extends DefaultExecutionGraphQlRequest implements
    ExecutionGraphQlRequest {

    private static final Map<String, List<HttpCookie>> EMPTY_COOKIES = new HashMap<>();

    private final URI uri;

    private final Map<String, List<String>> headers;

    private final Map<String, List<HttpCookie>> cookies;

    private final Map<String, Object> attributes;


    /**
     * Create an instance.
     *
     * @deprecated as of 1.1.3 in favor of the constructor with cookies
     */
    @Deprecated
    public WebGraphQlRequest(URI uri, Map<String, List<String>> headers, Map<String, Object> body,
        String id,
        @Nullable Locale locale) {
        this(uri, headers, null, Collections.emptyMap(), body, id, locale);
    }

    /**
     * Create an instance.
     *
     * @param uri the URL for the HTTP request or WebSocket handshake
     * @param headers the HTTP request headers
     * @param cookies the HTTP request cookies
     * @param attributes request attributes
     * @param body the deserialized content of the GraphQL request
     * @param id an identifier for the GraphQL request
     * @param locale the locale from the HTTP request, if any
     * @since 1.1.3
     */
    public WebGraphQlRequest(
        URI uri, Map<String, List<String>> headers, @Nullable Map<String, List<HttpCookie>> cookies,
        Map<String, Object> attributes, Map<String, Object> body, String id,
        @Nullable Locale locale) {
        super(getKey("query", body), getKey("operationName", body), getKey("variables", body),
            getKey("extensions", body), id, locale);

        Assert.notNull(uri, "URI is required'");
        Assert.notNull(headers, "HttpHeaders is required'");

        this.uri = uri;
        this.headers = headers;
        this.cookies = (cookies != null ? Collections.unmodifiableMap(cookies)
            : EMPTY_COOKIES);
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getKey(String key, Map<String, Object> body) {
        if (key.equals("query") && StringUtils.isBlank((String) body.get(key))) {
            throw new IllegalStateException("No \"query\" in the request document");
        }
        return (T) body.get(key);
    }


    /**
     * Return the URL for the HTTP request or WebSocket handshake.
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Return the HTTP headers of the request or WebSocket handshake.
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * Return the cookies of the request of WebSocket handshake.
     *
     * @since 1.1.3
     */
    public Map<String, List<HttpCookie>> getCookies() {
        return this.cookies;
    }

    /**
     * Return the request or WebSocket session attributes.
     *
     * @since 1.1.3
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

}

