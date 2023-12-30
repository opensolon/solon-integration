package graphql.solon.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import graphql.solon.controller.GraphqlController;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import test.App;

/**
 * 测试 Schema 查询
 *
 * @author fuzi1996
 * @since 2.6.3
 */
@ExtendWith(SolonJUnit5Extension.class)
@SolonTest(value = App.class, properties = "tbk=demo.book.component")
public class SchemaRequestTest extends HttpTester {

    /**
     * @see GraphqlController#getSchema(java.lang.Object, org.noear.solon.core.handle.Context)
     */
    @Test
    public void testSchemaReqeust() throws IOException {
        String content = path("/schema").bodyTxt("").post();
        assertThat(content, notNullValue());
    }
}
