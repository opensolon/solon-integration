package org.hibernate.solon.integration.query;

import org.hibernate.query.Query;
import org.noear.solon.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态查询构建器
 * 
 * <p>用于构建动态HQL查询，支持条件拼接、参数绑定等</p>
 * 
 * <pre>
 * DynamicQueryBuilder builder = new DynamicQueryBuilder("FROM User u");
 * builder.where("u.name = :name", "name", "张三")
 *        .where("u.age > :age", "age", 18)
 *        .orderBy("u.createTime DESC");
 * String hql = builder.build();
 * Map&lt;String, Object&gt; params = builder.getParameters();
 * </pre>
 * 
 * @author noear
 * @since 3.4
 */
public class DynamicQueryBuilder {
    
    private final StringBuilder hql;
    private final Map<String, Object> parameters;
    private final List<String> whereConditions;
    private final List<String> orderByClauses;
    
    /**
     * 构造函数
     * 
     * @param baseQuery 基础查询语句（如：FROM User u）
     */
    public DynamicQueryBuilder(String baseQuery) {
        this.hql = new StringBuilder(baseQuery);
        this.parameters = new HashMap<>();
        this.whereConditions = new ArrayList<>();
        this.orderByClauses = new ArrayList<>();
    }
    
    /**
     * 添加WHERE条件
     * 
     * @param condition 条件语句（如：u.name = :name）
     * @param paramName 参数名
     * @param paramValue 参数值
     * @return 当前构建器
     */
    public DynamicQueryBuilder where(String condition, String paramName, Object paramValue) {
        if (paramValue != null) {
            whereConditions.add(condition);
            parameters.put(paramName, paramValue);
        }
        return this;
    }
    
    /**
     * 添加WHERE条件（不检查参数值）
     * 
     * @param condition 条件语句
     * @return 当前构建器
     */
    public DynamicQueryBuilder where(String condition) {
        whereConditions.add(condition);
        return this;
    }
    
    /**
     * 添加WHERE条件（带参数）
     * 
     * @param condition 条件语句
     * @param parameters 参数Map
     * @return 当前构建器
     */
    public DynamicQueryBuilder where(String condition, Map<String, Object> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            whereConditions.add(condition);
            this.parameters.putAll(parameters);
        }
        return this;
    }
    
    /**
     * 添加LIKE条件
     * 
     * @param field 字段（如：u.name）
     * @param paramName 参数名
     * @param paramValue 参数值（会自动添加%通配符）
     * @return 当前构建器
     */
    public DynamicQueryBuilder like(String field, String paramName, String paramValue) {
        if (Utils.isNotEmpty(paramValue)) {
            whereConditions.add(field + " LIKE :" + paramName);
            parameters.put(paramName, "%" + paramValue + "%");
        }
        return this;
    }
    
    /**
     * 添加IN条件
     * 
     * @param field 字段
     * @param paramName 参数名
     * @param paramValues 参数值列表
     * @return 当前构建器
     */
    public DynamicQueryBuilder in(String field, String paramName, List<?> paramValues) {
        if (paramValues != null && !paramValues.isEmpty()) {
            whereConditions.add(field + " IN :" + paramName);
            parameters.put(paramName, paramValues);
        }
        return this;
    }
    
    /**
     * 添加BETWEEN条件
     * 
     * @param field 字段
     * @param paramNameStart 起始参数名
     * @param startValue 起始值
     * @param paramNameEnd 结束参数名
     * @param endValue 结束值
     * @return 当前构建器
     */
    public DynamicQueryBuilder between(String field, String paramNameStart, Object startValue, 
                                       String paramNameEnd, Object endValue) {
        if (startValue != null && endValue != null) {
            whereConditions.add(field + " BETWEEN :" + paramNameStart + " AND :" + paramNameEnd);
            parameters.put(paramNameStart, startValue);
            parameters.put(paramNameEnd, endValue);
        }
        return this;
    }
    
    /**
     * 添加ORDER BY子句
     * 
     * @param orderBy ORDER BY子句（如：u.createTime DESC）
     * @return 当前构建器
     */
    public DynamicQueryBuilder orderBy(String orderBy) {
        if (Utils.isNotEmpty(orderBy)) {
            orderByClauses.add(orderBy);
        }
        return this;
    }
    
    /**
     * 构建完整的HQL查询语句
     * 
     * @return HQL查询语句
     */
    public String build() {
        StringBuilder result = new StringBuilder(hql);
        
        // 添加WHERE子句
        if (!whereConditions.isEmpty()) {
            result.append(" WHERE ");
            for (int i = 0; i < whereConditions.size(); i++) {
                if (i > 0) {
                    result.append(" AND ");
                }
                result.append(whereConditions.get(i));
            }
        }
        
        // 添加ORDER BY子句
        if (!orderByClauses.isEmpty()) {
            result.append(" ORDER BY ");
            for (int i = 0; i < orderByClauses.size(); i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(orderByClauses.get(i));
            }
        }
        
        return result.toString();
    }
    
    /**
     * 获取查询参数
     * 
     * @return 参数Map
     */
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    /**
     * 应用参数到Query对象
     * 
     * @param query Query对象
     * @return Query对象
     */
    public <T> Query<T> applyParameters(Query<T> query) {
        parameters.forEach(query::setParameter);
        return query;
    }
}

