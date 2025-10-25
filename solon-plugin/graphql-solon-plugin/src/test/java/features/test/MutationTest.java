package features.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.alibaba.fastjson.JSON;
import demo.App;
import demo.product.dto.ProductPriceHistoryDTO;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import org.noear.snack4.ONode;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonTest;

/**
 * 测试修改和订阅
 *
 * @author lagnx
 */
@SolonTest(value = App.class)
public class MutationTest extends HttpTester {

    @Test
    public void testAddMutation() throws IOException {
        ProductPriceHistoryDTO product = new ProductPriceHistoryDTO(123L, new Date(), 999);
        ONode oNode = addProduct(product);

        // addProduct 指定义的 GraphQL 操作名称
        assertThat(oNode.get("addProduct").isNull(), is(false));
        assertThat(oNode.select("$.addProduct.id").getLong(), is(product.getId()));
        assertThat(oNode.select("$.addProduct.price").getInt(), is(product.getPrice()));
        assertThat(oNode.select("$.addProduct.startDate").getString(), is(product.getStartDate().toString()));
    }

    @Test
    public void testRemoveMutation() throws IOException {
        ProductPriceHistoryDTO product = new ProductPriceHistoryDTO(123L, new Date(), 999);
        addProduct(product);
        ONode oNode = removeProduct(product.getId());

        assertThat(oNode.get("removeProduct").isNull(), is(false));
        assertThat(oNode.select("$.removeProduct.id").getLong(), is(product.getId()));
        assertThat(oNode.select("$.removeProduct.price").getInt(), is(product.getPrice()));
        assertThat(oNode.select("$.removeProduct.startDate").getString(), is(product.getStartDate().toString()));
    }
    
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
        return ONode.ofJson(content).get("data");
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
        return ONode.ofJson(content).get("data");
    }
}
