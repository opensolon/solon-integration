package graphql.solon.event;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.solon.GraphqlPlugin;
import graphql.solon.configurer.RuntimeWiringConfigurer;
import graphql.solon.configurer.RuntimeWiringConfigurerCollect;
import graphql.solon.configurer.ThreadLocalAccessorCollect;
import graphql.solon.configurer.WebGraphQlInterceptorCollect;
import graphql.solon.execution.DataLoaderRegistrar;
import graphql.solon.execution.DefaultExecutionGraphQlService;
import graphql.solon.execution.DefaultSchemaResourceGraphQlSourceBuilder;
import graphql.solon.execution.GraphQlSource;
import graphql.solon.resolver.resource.GraphqlResourceResolver;
import graphql.solon.resolver.resource.GraphqlResourceResolverCollect;
import graphql.solon.resource.Resource;
import graphql.solon.support.ExecutionGraphQlService;
import graphql.solon.support.WebGraphQlHandler;
import graphql.solon.support.WebGraphQlHandlerGetter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fuzi1996
 * @since 2.3
 */
public class AppLoadEndEventListener implements EventListener<AppLoadEndEvent> {

    private static Logger log = LoggerFactory.getLogger(GraphqlPlugin.class);

    @Override
    public void onEvent(AppLoadEndEvent appLoadEndEvent) throws Throwable {
        this.initGraphqlSource(appLoadEndEvent);
    }

    private void initGraphqlSource(AppLoadEndEvent appLoadEndEvent) {
        GraphqlResourceResolverCollect graphqlResourceResolverCollect = new GraphqlResourceResolverCollect();
        RuntimeWiringConfigurerCollect runtimeWiringConfigurerCollect = new RuntimeWiringConfigurerCollect();
        ThreadLocalAccessorCollect threadLocalAccessorCollect = new ThreadLocalAccessorCollect();
        WebGraphQlInterceptorCollect webGraphQlInterceptorCollect = new WebGraphQlInterceptorCollect();

        EventBus.publish(graphqlResourceResolverCollect);
        EventBus.publish(runtimeWiringConfigurerCollect);
        EventBus.publish(threadLocalAccessorCollect);
        EventBus.publish(webGraphQlInterceptorCollect);

        AppContext appContext = appLoadEndEvent.context();

        GraphQlSource graphQlSource = appContext.getBean(GraphQlSource.class);
        Set<Resource> resources = new LinkedHashSet<>();
        List<GraphqlResourceResolver> resolvers = graphqlResourceResolverCollect
            .getAllCollector();
        if (Objects.nonNull(resolvers)) {
            resolvers.forEach(resolver -> {
                if (resolver.isNeedAppend(resources)) {
                    Set<Resource> otherResources = resolver.getGraphqlResource();
                    resources.addAll(otherResources);
                }
            });
        }

        List<RuntimeWiringConfigurer> configurers = runtimeWiringConfigurerCollect
                .getAllCollector();

        DefaultSchemaResourceGraphQlSourceBuilder defaultBuilder = new DefaultSchemaResourceGraphQlSourceBuilder();
        defaultBuilder.schemaResources(resources);

        if (Objects.nonNull(configurers)) {
            configurers.forEach(defaultBuilder::configureRuntimeWiring);
        }

        GraphQLSchema graphQlSchema = defaultBuilder.getGraphQlSchema();
        GraphQL graphql = GraphQL.newGraphQL(graphQlSchema).build();

        List<DataLoaderRegistrar> dataLoaderRegistrars = appContext
            .getBeansOfType(DataLoaderRegistrar.class);

        log.debug("默认的 GraphQlSource 初始化");
        graphQlSource.init(graphql, graphQlSchema, dataLoaderRegistrars);

        WebGraphQlHandlerGetter getter = appContext.getBean(WebGraphQlHandlerGetter.class);
        ExecutionGraphQlService service = new DefaultExecutionGraphQlService(graphQlSource);
        getter.setGraphQlHandler(WebGraphQlHandler.builder(service)
            .interceptors(webGraphQlInterceptorCollect.getAllCollector())
            .threadLocalAccessors(threadLocalAccessorCollect.getAllCollector())
            .build());
    }
}
