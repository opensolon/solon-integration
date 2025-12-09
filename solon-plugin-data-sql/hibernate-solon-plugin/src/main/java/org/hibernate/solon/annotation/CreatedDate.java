package org.hibernate.solon.annotation;

import java.lang.annotation.*;

/**
 * 创建时间注解
 * 
 * <p>用于标记实体类的创建时间字段，在插入时自动设置</p>
 * 
 * <pre>
 * &#64;Entity
 * public class User {
 *     &#64;CreatedDate
 *     private LocalDateTime createTime;
 * }
 * </pre>
 * 
 * @author noear
 * @since 3.4
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CreatedDate {
}

