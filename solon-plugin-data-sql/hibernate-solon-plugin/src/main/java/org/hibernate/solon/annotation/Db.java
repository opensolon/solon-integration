package org.hibernate.solon.annotation;

import java.lang.annotation.*;

/**
 * 数据工厂注解
 *
 * @deprecated 3.2 {@link org.noear.solon.data.annotation.Ds}
 * */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Db {
    /**
     * ds bean name
     * */
    String value() default "";
}