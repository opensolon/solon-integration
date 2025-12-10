package org.hibernate.solon.integration;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.noear.solon.Utils;
import org.noear.solon.core.util.ResourceUtil;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Hibernate配置类（继承自Hibernate的Configuration）
 * 
 * <p>用于构建Hibernate的SessionFactory，提供便捷的配置方法</p>
 * 
 * @author lingkang
 * @author bai
 * @since 2.5
 */
public class HibernateConfiguration extends Configuration {
    
    /**
     * 保存已注册的实体类列表（用于DDL生成）
     */
    private final List<Class<?>> registeredClasses = new ArrayList<>();

    public HibernateConfiguration() {
        super();
    }
    
    /**
     * 获取已注册的实体类列表
     */
    public List<Class<?>> getRegisteredClasses() {
        return new ArrayList<>(registeredClasses);
    }

    /**
     * 添加实体映射，有hibernate的@Table、@Entity
     */
    public HibernateConfiguration addMapping(String basePackage) {
        if (Utils.isNotEmpty(basePackage)) {
            Collection<Class<?>> classes = ResourceUtil.scanClasses(basePackage);
            for (Class<?> clazz : classes) {
                addAnnotatedClass(clazz);
                registeredClasses.add(clazz);
            }
        }
        return this;
    }
    
    /**
     * 添加实体类（重写以保存类列表）
     */
    @Override
    public Configuration addAnnotatedClass(Class annotatedClass) {
        super.addAnnotatedClass(annotatedClass);
        if (!registeredClasses.contains(annotatedClass)) {
            registeredClasses.add(annotatedClass);
        }
        return this;
    }

    /**
     * 设置数据源
     */
    public HibernateConfiguration setDataSource(DataSource dataSource) {
        if (dataSource != null) {
            this.getProperties().put(AvailableSettings.DATASOURCE, dataSource);
        }
        return this;
    }

    /**
     * 设置属性
     */
    public HibernateConfiguration setProperties(Properties properties) {
        if (properties != null) {
            properties.entrySet().forEach(obj -> {
                getProperties().put(obj.getKey(), obj.getValue());
            });
        }
        return this;
    }

    /**
     * 构建会话工具
     */
    @Override
    public SessionFactory buildSessionFactory() throws HibernateException {
        getProperties().put(AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY, "jdbc");
        getProperties().put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, ThreadLocalSessionContext.class.getName());

        SessionFactory sessionFactory = super.buildSessionFactory();
        return new JpaTranSessionFactory(sessionFactory);
    }
}
