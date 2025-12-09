package org.hibernate.solon.integration.query;

import org.noear.solon.core.util.ResourceUtil;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.HashMap;
import java.util.Map;

/**
 * 命名查询注册表
 * 
 * <p>自动扫描并注册@NamedQuery和@NamedQueries注解的查询</p>
 * 
 * @author noear
 * @since 3.4
 */
public class NamedQueryRegistry {
    
    private static final Map<String, String> namedQueries = new HashMap<>();
    
    /**
     * 注册实体类的命名查询
     * 
     * @param entityClass 实体类
     */
    public static void register(Class<?> entityClass) {
        // 注册单个@NamedQuery
        NamedQuery namedQuery = entityClass.getAnnotation(NamedQuery.class);
        if (namedQuery != null) {
            registerQuery(namedQuery.name(), namedQuery.query());
        }
        
        // 注册@NamedQueries
        NamedQueries namedQueries = entityClass.getAnnotation(NamedQueries.class);
        if (namedQueries != null) {
            for (NamedQuery nq : namedQueries.value()) {
                registerQuery(nq.name(), nq.query());
            }
        }
    }
    
    /**
     * 注册命名查询
     * 
     * @param name 查询名称
     * @param query HQL查询语句
     */
    public static void registerQuery(String name, String query) {
        if (name != null && query != null) {
            namedQueries.put(name, query);
        }
    }
    
    /**
     * 获取命名查询
     * 
     * @param name 查询名称
     * @return HQL查询语句，如果不存在返回null
     */
    public static String getQuery(String name) {
        return namedQueries.get(name);
    }
    
    /**
     * 检查命名查询是否存在
     * 
     * @param name 查询名称
     * @return 是否存在
     */
    public static boolean hasQuery(String name) {
        return namedQueries.containsKey(name);
    }
    
    /**
     * 扫描包下的所有实体类并注册命名查询
     * 
     * @param basePackage 基础包路径
     */
    @SuppressWarnings("deprecation")
    public static void scanAndRegister(String basePackage) {
        if (basePackage == null || basePackage.isEmpty()) {
            return;
        }
        
        java.util.Collection<Class<?>> classes = ResourceUtil.scanClasses(basePackage);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(javax.persistence.Entity.class) ||
                clazz.isAnnotationPresent(org.hibernate.annotations.Entity.class)) {
                register(clazz);
            }
        }
    }
    
    /**
     * 获取所有已注册的查询名称
     * 
     * @return 查询名称集合
     */
    public static java.util.Set<String> getAllQueryNames() {
        return new java.util.HashSet<>(namedQueries.keySet());
    }
    
    /**
     * 清空所有注册的查询
     */
    public static void clear() {
        namedQueries.clear();
    }
}

