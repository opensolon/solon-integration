package org.hibernate.solon.integration;

import org.hibernate.solon.annotation.EnableHibernate;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hibernate 自动配置类
 * 
 * <p>处理@EnableHibernate注解的配置，自动扫描并注册实体类</p>
 * 
 * @author noear
 * @since 3.4
 */
@Configuration
public class HibernateAutoConfiguration {
    
    /**
     * 初始化Hibernate配置
     * 
     * <p>扫描@EnableHibernate注解，自动配置实体类映射</p>
     */
    @Bean
    public void init() {
        EnableHibernate enableHibernate = Solon.app().source().getAnnotation(EnableHibernate.class);
        
        if (enableHibernate == null) {
            // 如果没有@EnableHibernate注解，不进行自动配置
            return;
        }
        
        // 收集需要扫描的包路径
        Set<String> packagesToScan = new HashSet<>();
        
        // 处理basePackages
        if (enableHibernate.basePackages().length > 0) {
            packagesToScan.addAll(Arrays.asList(enableHibernate.basePackages()));
        }
        
        // 处理basePackageClasses
        for (Class<?> clazz : enableHibernate.basePackageClasses()) {
            packagesToScan.add(clazz.getPackage().getName());
        }
        
        // 如果没有指定包，使用启动类所在的包
        if (packagesToScan.isEmpty()) {
            String defaultPackage = Solon.app().source().getPackageName();
            if (defaultPackage != null && !defaultPackage.isEmpty()) {
                packagesToScan.add(defaultPackage);
            }
        }
        
        // 如果启用了自动扫描
        if (enableHibernate.autoScanEntities() && !packagesToScan.isEmpty()) {
            // 延迟到所有数据源注册完成后执行
            Solon.app().onEvent(org.noear.solon.core.event.AppLoadEndEvent.class, e -> {
                scanAndRegisterEntities(packagesToScan);
            });
        }
        
        // 配置showSql
        if (enableHibernate.showSql()) {
            Solon.cfg().put("jpa.properties.hibernate.show_sql", "true");
        }
    }
    
    /**
     * 扫描并注册实体类
     */
    @SuppressWarnings("deprecation")
    private void scanAndRegisterEntities(Set<String> packagesToScan) {
        List<Class<?>> entityClasses = new ArrayList<>();
        
        for (String packageName : packagesToScan) {
            // 扫描包下的所有类
            java.util.Collection<Class<?>> classes = ResourceUtil.scanClasses(packageName);
            
            for (Class<?> clazz : classes) {
                // 检查是否有@Entity注解
                if (clazz.isAnnotationPresent(javax.persistence.Entity.class) ||
                    clazz.isAnnotationPresent(org.hibernate.annotations.Entity.class)) {
                    entityClasses.add(clazz);
                }
            }
        }
        
        // 将所有实体类添加到Hibernate配置中
        HibernateAdapterManager.getAll().values().forEach(adapter -> {
            for (Class<?> entityClass : entityClasses) {
                adapter.getConfiguration().addAnnotatedClass(entityClass);
            }
        });
    }
}

