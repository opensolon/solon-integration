package graphql.solon.resolver.argument;

import graphql.schema.DataFetchingEnvironment;
import java.lang.reflect.Method;

import org.noear.eggg.ParamEggg;
import org.noear.solon.lang.Nullable;

/**
 * @author fuzi1996
 * @since 2.3
 */
public interface HandlerMethodArgumentResolver {

    boolean supportsParameter(Method method, ParamEggg paramEggg);

    @Nullable
    Object resolveArgument(DataFetchingEnvironment environment, ParamEggg paramEggg, int index) throws Exception;
}