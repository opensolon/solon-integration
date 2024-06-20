/*
 * Copyright 2002-2022 the original author or authors.
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

package graphql.solon.execution;

import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.execution.ExecutionIdProvider;
import graphql.solon.support.DefaultExecutionGraphQlResponse;
import graphql.solon.support.ExecutionGraphQlRequest;
import graphql.solon.support.ExecutionGraphQlResponse;
import graphql.solon.support.ExecutionGraphQlService;
import java.util.function.BiFunction;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ContextEmpty;
import reactor.core.publisher.Mono;

/**
 * {@link ExecutionGraphQlService} that uses a {@link GraphQlSource} to obtain a
 * {@link GraphQL} instance and perform query execution.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
public class DefaultExecutionGraphQlService implements ExecutionGraphQlService {

    private static final BiFunction<ExecutionInput, ExecutionInput.Builder, ExecutionInput> RESET_EXECUTION_ID_CONFIGURER =
        (executionInput, builder) -> builder.executionId(null).build();


    private final GraphQlSource graphQlSource;

    private final boolean isDefaultExecutionIdProvider;


    public DefaultExecutionGraphQlService(GraphQlSource graphQlSource) {
        this.graphQlSource = graphQlSource;
        this.isDefaultExecutionIdProvider =
            (graphQlSource.graphQl().getIdProvider()
                == ExecutionIdProvider.DEFAULT_EXECUTION_ID_PROVIDER);
    }

    @Override
    public final Mono<ExecutionGraphQlResponse> execute(ExecutionGraphQlRequest request) {
        return Mono.deferContextual((contextView) -> {
            if (!this.isDefaultExecutionIdProvider && request.getExecutionId() == null) {
                request.configureExecutionInput(RESET_EXECUTION_ID_CONFIGURER);
            }
            ExecutionInput executionInput = request.toExecutionInput();
            GraphQLContext graphQLContext = executionInput.getGraphQLContext();
            graphQLContext.put(Context.class, ContextEmpty.create());
            ReactorContextManager
                .setReactorContext(contextView, executionInput.getGraphQLContext());
            ExecutionInput updatedExecutionInput = registerDataLoaders(executionInput);
            return Mono.fromFuture(this.graphQlSource.graphQl().executeAsync(updatedExecutionInput))
                .map(result -> new DefaultExecutionGraphQlResponse(updatedExecutionInput, result));
        });
    }

    private ExecutionInput registerDataLoaders(ExecutionInput executionInput) {
        ExecutionInput newExecutionInput = this.graphQlSource.registerDataLoaders(executionInput);
        return newExecutionInput;
    }

}
