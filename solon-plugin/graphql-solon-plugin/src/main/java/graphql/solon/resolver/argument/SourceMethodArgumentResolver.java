package graphql.solon.resolver.argument;

import graphql.schema.DataFetchingEnvironment;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.lang3.ClassUtils;
import org.noear.eggg.ParamEggg;
import org.noear.solon.annotation.Param;

/**
 * @author fuzi1996
 * @since 2.3
 */
public class SourceMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(Method method, ParamEggg paramEggg) {
        Class<?> type = paramEggg.getType();
        Parameter parameter = paramEggg.getParam();
        Param annotation = parameter.getDeclaredAnnotation(Param.class);
        return (Objects.isNull(annotation)
                && !isExcludedSimpleValueType(type)
                && !type.isArray()
                && !Collection.class.isAssignableFrom(type));
    }

    private static boolean isExcludedSimpleValueType(Class<?> type) {
        // Same as BeanUtils.isSimpleValueType except for CharSequence and Number
        return (Void.class != type && void.class != type &&
                (ClassUtils.isPrimitiveOrWrapper(type) ||
                        Enum.class.isAssignableFrom(type) ||
                        Date.class.isAssignableFrom(type) ||
                        Temporal.class.isAssignableFrom(type) ||
                        URI.class == type ||
                        URL.class == type ||
                        Locale.class == type ||
                        Class.class == type));
    }

    @Override
    public Object resolveArgument(DataFetchingEnvironment environment, ParamEggg paramEggg, int index) throws Exception {
        Object source = environment.getSource();
        Class<?> type = paramEggg.getType();
        if (!type.isInstance(source)) {
            throw new IllegalArgumentException(
                    "The declared parameter of type '" + type.toGenericString() + "' " +
                            "does not match the type of the source Object '" + source.getClass()
                            + "'.");
        }
        return source;
    }
}
