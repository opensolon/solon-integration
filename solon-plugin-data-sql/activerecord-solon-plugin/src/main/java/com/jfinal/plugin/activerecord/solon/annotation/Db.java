package com.jfinal.plugin.activerecord.solon.annotation;

import java.lang.annotation.*;


/**
 * 数据库注解
 *
 * @author 胡高 (https://gitee.com/gollyhu)
 * @since 1.10
 * @deprecated 3.2 {@link org.noear.solon.data.annotation.Ds}
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface Db {
    /**
     * 数据源Bean实例名称
     */
    String value() default "";
}