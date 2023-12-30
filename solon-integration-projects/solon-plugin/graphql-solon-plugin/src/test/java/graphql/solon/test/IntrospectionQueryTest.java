package graphql.solon.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import demo.App;
import graphql.solon.controller.GraphqlController;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.snack.ONode;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;

/**
 * 测试 IntrospectionQuery 查询
 *
 * @author fuzi1996
 * @since 2.6.3
 */
@ExtendWith(SolonJUnit5Extension.class)
@SolonTest(value = App.class, properties = "tbk=demo.book.component")
public class IntrospectionQueryTest extends HttpTester {

    /**
     * @see GraphqlController#graphql(graphql.solon.resource.GraphqlRequestParam, org.noear.solon.core.handle.Context)
     */
    @Test
    public void testQuery() throws IOException {
        ONode param = new ONode();
        param.set("query",
            ResourceUtil.getResourceAsString("/query/introspectionQuery.gqls"));
        String json = param.toJson();

        String content = path("/graphql").bodyJson(json).post();
        ONode oNode = ONode.loadStr(content).get("data");
        ONode schema = oNode.get("__schema");
        assertThat(schema.isNull(), is(false));

        assertThat(schema.get("queryType").isNull(), is(false));
        assertThat(schema.get("mutationType").isNull(), is(false));
        assertThat(schema.get("types").isNull(), is(false));
        assertThat(schema.get("directives").isNull(), is(false));
    }
}
