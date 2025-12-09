package org.hibernate.solon.annotation;

import java.lang.annotation.*;

/**
 * 缓存注解
 * 
 * <p>用于标记实体类或查询方法是否启用缓存</p>
 * 
 * <pre>
 * &#64;Entity
 * &#64;Cacheable
 * &#64;Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 * public class User {
 *     // ...
 * }
 * </pre>
 * 
 * @author noear
 * @since 3.4
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {
    /**
     * 是否启用缓存
     */
    boolean value() default true;
    
    /**
     * 缓存区域名称
     */
    String region() default "";
    
    /**
     * 缓存策略
     * READ_ONLY: 只读，适用于不经常修改的数据
     * READ_WRITE: 读写，适用于经常修改的数据
     * NONSTRICT_READ_WRITE: 非严格读写
     * TRANSACTIONAL: 事务性缓存
     */
    CacheStrategy strategy() default CacheStrategy.READ_WRITE;
    
    /**
     * 缓存策略枚举
     */
    enum CacheStrategy {
        READ_ONLY,
        READ_WRITE,
        NONSTRICT_READ_WRITE,
        TRANSACTIONAL
    }
}

