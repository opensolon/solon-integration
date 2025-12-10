package org.hibernate.solon.integration.query;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Map;

/**
 * Hibernate查询助手类
 * 
 * <p>提供便捷的查询方法，包括分页查询、条件查询等</p>
 * 
 * @author noear
 * @since 3.4
 */
public class HibernateQueryHelper {
    
    private final Session session;
    
    public HibernateQueryHelper(Session session) {
        this.session = session;
    }
    
    /**
     * 创建HQL查询
     */
    public <T> Query<T> createQuery(String hql, Class<T> resultClass) {
        return session.createQuery(hql, resultClass);
    }
    
    /**
     * 创建原生SQL查询
     */
    public Query<?> createNativeQuery(String sql) {
        return session.createNativeQuery(sql);
    }
    
    /**
     * 创建原生SQL查询（指定结果类型）
     */
    public <T> Query<T> createNativeQuery(String sql, Class<T> resultClass) {
        return session.createNativeQuery(sql, resultClass);
    }
    
    /**
     * 执行HQL查询并返回列表
     */
    public <T> List<T> list(String hql, Class<T> resultClass) {
        return createQuery(hql, resultClass).list();
    }
    
    /**
     * 执行HQL查询并返回列表（带参数）
     */
    public <T> List<T> list(String hql, Class<T> resultClass, Map<String, Object> parameters) {
        Query<T> query = createQuery(hql, resultClass);
        if (parameters != null) {
            parameters.forEach(query::setParameter);
        }
        return query.list();
    }
    
    /**
     * 执行HQL查询并返回单个结果
     */
    public <T> T uniqueResult(String hql, Class<T> resultClass) {
        return createQuery(hql, resultClass).uniqueResult();
    }
    
    /**
     * 执行HQL查询并返回单个结果（带参数）
     */
    public <T> T uniqueResult(String hql, Class<T> resultClass, Map<String, Object> parameters) {
        Query<T> query = createQuery(hql, resultClass);
        if (parameters != null) {
            parameters.forEach(query::setParameter);
        }
        return query.uniqueResult();
    }
    
    /**
     * 分页查询
     * 
     * @param hql HQL查询语句
     * @param resultClass 结果类型
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    public <T> PageQuery<T> pageQuery(String hql, Class<T> resultClass, int page, int size) {
        return pageQuery(hql, resultClass, null, page, size);
    }
    
    /**
     * 分页查询（带参数）
     * 
     * @param hql HQL查询语句
     * @param resultClass 结果类型
     * @param parameters 查询参数
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    public <T> PageQuery<T> pageQuery(String hql, Class<T> resultClass, Map<String, Object> parameters, int page, int size) {
        // 构建计数查询
        String countHql = buildCountQuery(hql);
        
        // 查询总数
        Query<Long> countQuery = createQuery(countHql, Long.class);
        if (parameters != null) {
            parameters.forEach(countQuery::setParameter);
        }
        Long total = countQuery.uniqueResult();
        if (total == null) {
            total = 0L;
        }
        
        // 查询数据
        Query<T> dataQuery = createQuery(hql, resultClass);
        if (parameters != null) {
            parameters.forEach(dataQuery::setParameter);
        }
        
        // 设置分页参数
        int offset = (page - 1) * size;
        dataQuery.setFirstResult(offset);
        dataQuery.setMaxResults(size);
        
        List<T> content = dataQuery.list();
        
        return new PageQuery<>(content, total, page, size);
    }
    
    /**
     * 构建计数查询语句
     */
    private String buildCountQuery(String hql) {
        String upperHql = hql.toUpperCase().trim();
        int fromIndex = upperHql.indexOf("FROM");
        
        if (fromIndex == -1) {
            throw new IllegalArgumentException("HQL查询必须包含FROM子句: " + hql);
        }
        
        // 提取FROM之后的内容
        String fromClause = hql.substring(fromIndex);
        
        // 移除ORDER BY子句（如果存在）
        int orderByIndex = upperHql.indexOf("ORDER BY");
        if (orderByIndex != -1) {
            fromClause = fromClause.substring(0, orderByIndex - fromIndex).trim();
        }
        
        return "SELECT COUNT(*) " + fromClause;
    }
    
    /**
     * 执行更新操作
     */
    public int executeUpdate(String hql) {
        return executeUpdate(hql, null);
    }
    
    /**
     * 执行更新操作（带参数）
     */
    public int executeUpdate(String hql, Map<String, Object> parameters) {
        Query<?> query = session.createQuery(hql);
        if (parameters != null) {
            parameters.forEach(query::setParameter);
        }
        return query.executeUpdate();
    }
    
    /**
     * 获取Session
     */
    public Session getSession() {
        return session;
    }
}

