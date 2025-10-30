package graphql.solon.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.solon.resolver.argument.HandlerMethodArgumentResolver;
import graphql.solon.resolver.argument.HandlerMethodArgumentResolverCollect;
import graphql.solon.util.ReflectionUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.noear.eggg.MethodEggg;
import org.noear.eggg.ParamEggg;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanWrap;

/**
 * @author fuzi1996
 * @since 2.3
 */
public class SchemaMappingDataFetcher implements DataFetcher<Object> {

    protected final AppContext context;
    protected final BeanWrap wrap;
    protected final MethodEggg methodEgg;
    protected final HandlerMethodArgumentResolverCollect collect;
    protected final Map<ParamEggg, HandlerMethodArgumentResolver> argumentResolverCache;
    protected final boolean isBatch;

    public SchemaMappingDataFetcher(AppContext context, BeanWrap wrap, MethodEggg methodEggg, boolean isBatch) {
        this.context = context;
        this.wrap = wrap;
        this.methodEgg = methodEggg;
        this.collect = this.context
                .getBean(HandlerMethodArgumentResolverCollect.class);
        this.argumentResolverCache = new ConcurrentHashMap<>(256);

        this.isBatch = isBatch;
    }

    /**
     * 构建执行参数
     */
    protected Object[] buildArgs(DataFetchingEnvironment environment) throws Exception {
        if (methodEgg.getParamCount() > 0) {
            Object[] arguments = new Object[methodEgg.getParamCount()];

            if (this.getMethodArgLength() > 0) {

                for (int i = 0; i < methodEgg.getParamCount(); i++) {
                    ParamEggg pe = methodEgg.getParamEgggAt(i);
                    arguments[i] = this.getArgument(environment, pe, i);
                }
            }

            return arguments;
        }
        return null;
    }

    protected Object getArgument(DataFetchingEnvironment environment, ParamEggg pe, int index) throws Exception {
        HandlerMethodArgumentResolver resolver = this.argumentResolverCache.get(pe);

        if (Objects.isNull(resolver)) {
            List<HandlerMethodArgumentResolver> allCollector = this.collect.getAllCollector();
            // 从后往前判断,这样可以使得后加的优先级高
            for (int i = allCollector.size() - 1; i >= 0; i--) {
                HandlerMethodArgumentResolver item = allCollector.get(i);
                if (item.supportsParameter(methodEgg.getMethod(), pe)) {
                    resolver = item;
                    this.argumentResolverCache.put(pe, resolver);
                    break;
                }
            }
        }

        if (Objects.nonNull(resolver)) {
            return resolver.resolveArgument(environment, pe, index);
        }

        throw new IllegalArgumentException("not support resolve method argument");
    }

    private int getMethodArgLength() {
        return methodEgg.getParamCount();
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        Object[] args = this.buildArgs(environment);
        return this.invokeMethod(args);
    }

    protected Object invokeMethod(Object[] args) {
        return ReflectionUtils.invokeMethod(methodEgg.getMethod(), wrap.get(), args);
    }
}
