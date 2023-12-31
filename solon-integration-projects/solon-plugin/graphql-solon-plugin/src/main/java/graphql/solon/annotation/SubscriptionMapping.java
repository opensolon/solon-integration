package graphql.solon.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.noear.solon.annotation.Alias;

/**
 * @author fuzi1996
 * @since 2.3
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SubscriptionMapping {

    @Alias("value")
    String name() default "";

    @Alias("name")
    String value() default "";

    String typeName() default "Subscription";
}
