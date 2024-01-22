package graphql.solon.controller;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.idl.SchemaPrinter;
import graphql.solon.execution.GraphQlSource;
import graphql.solon.resource.GraphqlRequestParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Context;

/**
 * @author fuzi1996
 * @since 2.3
 */
@Controller
public class GraphqlController {

    @Inject
    private GraphQlSource graphQlSource;

    @Post
    @Mapping("/graphql")
    public Map<String, Object> graphql(GraphqlRequestParam param, Context ctx) {
        GraphQL graphQL = this.graphQlSource.graphQl();

        Map<Object, Object> mapOfContext = new HashMap<>();
        mapOfContext.put(Context.class, ctx);

        ExecutionInput executionInput = ExecutionInput
            .newExecutionInput()
            .query(param.getQuery())
            .operationName(param.getOperationName() == null ? "" : param.getOperationName())
            .localContext(ctx.getLocale())
            .graphQLContext(mapOfContext)
            .variables(param.getVariables() == null ? Collections.emptyMap() : param.getVariables())
            .build();

        executionInput = this.graphQlSource.registerDataLoaders(executionInput);

        ExecutionResult executionResult = graphQL.execute(executionInput);
        return executionResult.toSpecification();
    }


    @Post
    @Mapping("/schema")
    public String getSchema() {
        return new SchemaPrinter().print(this.graphQlSource.schema());
    }
}
