package org.hibernate.solon.annotation;

import org.hibernate.solon.integration.HibernateAutoConfiguration;
import org.noear.solon.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 Hibernate 支持
 * 
 * <p>在启动类上使用此注解来启用Hibernate功能，支持自动扫描实体类</p>
 * 
 * <pre>
 * &#64;EnableHibernate(basePackages = "com.example.entity")
 * public class App {
 *     public static void main(String[] args) {
 *         Solon.start(App.class, args);
 *     }
 * }
 * </pre>
 * 
 * @author noear
 * @since 3.4
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HibernateAutoConfiguration.class)
public @interface EnableHibernate {
    /**
     * 实体类扫描的基础包路径（支持多个）
     * 
     * <p>如果不指定，将扫描启动类所在包及其子包</p>
     */
    String[] basePackages() default {};
    
    /**
     * 通过类来指定扫描的基础包（支持多个）
     * 
     * <p>将扫描这些类所在包及其子包</p>
     */
    Class<?>[] basePackageClasses() default {};
    
    /**
     * 是否启用自动扫描实体类
     * 
     * <p>默认为true，会自动扫描basePackages下的@Entity类</p>
     */
    boolean autoScanEntities() default true;
    
    /**
     * 是否显示SQL日志
     * 
     * <p>默认为false，可通过配置文件的jpa.show_sql覆盖</p>
     */
    boolean showSql() default false;
}

