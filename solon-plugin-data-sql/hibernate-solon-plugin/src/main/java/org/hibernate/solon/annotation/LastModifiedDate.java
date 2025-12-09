package org.hibernate.solon.annotation;

import java.lang.annotation.*;

/**
 * 最后修改时间注解
 * 
 * <p>用于标记实体类的最后修改时间字段，在插入和更新时自动设置</p>
 * 
 * <pre>
 * &#64;Entity
 * public class User {
 *     &#64;LastModifiedDate
 *     private LocalDateTime updateTime;
 * }
 * </pre>
 * 
 * @author noear
 * @since 3.4
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LastModifiedDate {
}

