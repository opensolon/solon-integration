package features.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.alibaba.fastjson.JSON;
import demo.App;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import demo.product.dto.ProductPriceHistoryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.java_websocket.client.SimpleWebSocketClient;
import org.noear.snack.ONode;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;

/**
 * 测试修改和订阅
 *
 * @author lagnx
 */
@ExtendWith(SolonJUnit5Extension.class)
@SolonTest(value = App.class)
public class MutationAndSubscriptionTest extends HttpTester {

    @Test
    public void testAddMutation() throws IOException {
        ProductPriceHistoryDTO product = new ProductPriceHistoryDTO(123L, new Date(), 999);
        ONode oNode = addProduct(product);

        // addProduct 指定义的 GraphQL 操作名称
        assertThat(oNode.get("addProduct").isNull(), is(false));
        assertThat(oNode.select("addProduct.id").getLong(), is(product.getId()));
        assertThat(oNode.select("addProduct.price").getInt(), is(product.getPrice()));
        assertThat(oNode.select("addProduct.startDate").getString(), is(product.getStartDate().toString()));
    }

    @Test
    public void testRemoveMutation() throws IOException {
        ProductPriceHistoryDTO product = new ProductPriceHistoryDTO(123L, new Date(), 999);
        addProduct(product);
        ONode oNode = removeProduct(product.getId());

        assertThat(oNode.get("removeProduct").isNull(), is(false));
        assertThat(oNode.select("removeProduct.id").getLong(), is(product.getId()));
        assertThat(oNode.select("removeProduct.price").getInt(), is(product.getPrice()));
        assertThat(oNode.select("removeProduct.startDate").getString(), is(product.getStartDate().toString()));
    }

    /**
     * 订阅测试：未完成！会失败
     */
    /*@Test
    public void testSubscription() throws IOException, InterruptedException {
        ProductPriceHistoryDTO product = new ProductPriceHistoryDTO(123L, new Date(), 999);

        ONode param = new ONode();
        ONode variables = new ONode();
        variables.set("productId", product.getId());
        param.set("query",
                ResourceUtil.getResourceAsString("/query/subscriptionProduct.gqls"));
        param.set("variables", variables);
        param.set("operationName", "testSubscription");
        String json = param.toJson();

        // socket
        SimpleWebSocketClient client = new SimpleWebSocketClient("ws://127.0.0.1:8081/graphql") {
            @Override
            public void onMessage(String message) {
                super.onMessage(message);
                System.out.println("get a message: " + message);
            }
        };

        client.connectBlocking(10, TimeUnit.SECONDS);
        //定制心跳（可选）
        //client.heartbeatHandler(new HeartbeatHandlerDefault());
        //开始心跳 + 心跳时自动重连
        client.heartbeat(20_000, true);

        //发送测试
        client.send(json);

        //休息会儿
        for(int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            System.out.println("====");
        }

        //关闭（使用 release 会同时停止心跳及自动重连）
        client.release();
    }*/

    private ONode addProduct(ProductPriceHistoryDTO product) throws IOException {
        ONode param = new ONode();
        ONode variables = new ONode();

        variables.set("product", JSON.parseObject(JSON.toJSONString(product), LinkedHashMap.class));

        param.set("query",
                ResourceUtil.getResourceAsString("/query/mutationAddProduct.gqls"));
        param.set("variables", variables);
        param.set("operationName", "testAdd");  // 有多个操作时，需指定需要执行的哪一个操作
        String json = param.toJson();

        String content = path("/graphql").bodyJson(json).post();
        return ONode.loadStr(content).get("data");
    }

    private ONode removeProduct(Long productId) throws IOException {
        ONode param = new ONode();
        ONode variables = new ONode();

        variables.set("productId", productId);

        param.set("query",
                ResourceUtil.getResourceAsString("/query/mutationRemoveProduct.gqls"));
        param.set("variables", variables);
        param.set("operationName", "testRemove");
        String json = param.toJson();

        String content = path("/graphql").bodyJson(json).post();
        return ONode.loadStr(content).get("data");
    }
}
