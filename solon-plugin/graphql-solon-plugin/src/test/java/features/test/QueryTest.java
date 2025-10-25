package features.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import demo.App;
import demo.book.component.BookService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.noear.snack4.ONode;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonTest;

/**
 * 测试普通的graphql查询
 *
 * @author fuzi1996
 * @since 2.3
 */
@SolonTest(value = App.class, properties = "tbk=demo.book.component")
public class QueryTest extends HttpTester {

    /**
     * @see BookService#bookById(java.lang.String)
     */
    @Test
    public void testQuery() throws IOException {
        ONode param = new ONode();
        ONode variables = new ONode();
        param.set("query",
                ResourceUtil.getResourceAsString("/query/queryBookById.gqls"));
        variables.set("id", "book-1");
        param.set("variables", variables);
        String json = param.toJson();

        String content = path("/graphql").bodyJson(json).post();
        ONode oNode = ONode.ofJson(content).get("data");

        assertThat(oNode.get("bookById").isNull(), is(false));
        assertThat(oNode.select("$.bookById.name").getString(), is("book-1"));
        assertThat(oNode.select("$.bookById.pageCount").getInt(), is(1));
        assertThat(oNode.select("$.bookById.author").isNull(), is(false));
        assertThat(oNode.select("$.bookById.author.firstName").getString(), is("J"));
        assertThat(oNode.select("$.bookById.author.lastName").getString(), is("K"));

    }
}
