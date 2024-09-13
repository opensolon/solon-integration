package features.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import demo.App;
import demo.product.dto.ProductPriceHistoryDTO;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.noear.java_websocket.client.SimpleWebSocketClient;
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
    public void testSubscription() throws IOException, InterruptedException {
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
            Thread.sleep(1000);
            System.out.println("====");
        }

        client.complete();

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            System.out.println("====");
        }

        //关闭（使用 release 会同时停止心跳及自动重连）
        client.release();
    }

    static class WsClient extends SimpleWebSocketClient {

        private String id;

        private boolean isConnected = false;

        private boolean isCompleted = false;

        public WsClient(String serverUri) {
            super(serverUri);
        }

        @Override
        public void onMessage(String message) {
            System.out.printf("--------- %s ---------%n", message);
            MesResult result = ONode.deserialize(message, MesResult.class);
            String type = result.getType();
            if (Objects.equals("connection_ack", type)) {
                this.isConnected = true;
            } else if (Objects.equals("next", type)) {
                String mesId = result.getId();
                if (Objects.equals(this.id, mesId)) {
                    if (!this.isCompleted) {
                        ONode payload = result.getPayload();
                        ONode data = payload.get("data");
                        assertThat(data, notNullValue());

                        ONode notifyProductPriceChange = data.get("notifyProductPriceChange");
                        assertThat(notifyProductPriceChange, notNullValue());

                        ONode price = notifyProductPriceChange.get("price");
                        Object priceValue = price.toData();
                        assertThat(priceValue, instanceOf(Number.class));
                    } else {
                        throw new IllegalArgumentException("订阅关闭不应该接收数据");
                    }
                } else {
                    throw new IllegalArgumentException("未知id");
                }
            } else {
                throw new IllegalArgumentException("未知数据");
            }
        }

        public void initConnection() throws InterruptedException {
            if (!this.isConnected) {
                this.connectBlocking();
                this.send("{\"type\":\"connection_init\",\"payload\":{}}");
            }
        }

        public void subscribe(ONode payload) {
            this.id = UUID.randomUUID().toString();
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
