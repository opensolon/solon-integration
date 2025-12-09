package org.hibernate.solon.integration;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.integration.batch.BatchOperationHelper;
import org.hibernate.solon.integration.query.DynamicQueryBuilder;
import org.hibernate.solon.integration.query.HibernateQueryHelper;
import org.hibernate.solon.integration.query.PageQuery;

import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hibernate Repository 基类
 * 
 * <p>提供基础的CRUD操作，简化Repository实现</p>
 * 
 * <pre>
 * &#64;Component
 * public class UserRepository extends HibernateRepository&lt;User, Long&gt; {
 *     public UserRepository() {
 *         super(User.class);
 *     }
 *     
 *     public List&lt;User&gt; findByName(String name) {
 *         return getSession()
 *             .createQuery("FROM User WHERE name = :name", entityClass)
 *             .setParameter("name", name)
 *             .list();
 *     }
 * }
 * </pre>
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author noear
 * @since 3.4
 */
public abstract class HibernateRepository<T, ID extends Serializable> {
    
    protected final Class<T> entityClass;
    
    @Db
    protected SessionFactory sessionFactory;
    
    @PersistenceContext
    protected javax.persistence.EntityManager entityManager;
    
    /**
     * 构造函数
     * 
     * @param entityClass 实体类
     */
    public HibernateRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    /**
     * 获取当前Session
     */
    protected Session getSession() {
        if (sessionFactory != null) {
            return sessionFactory.getCurrentSession();
        }
        if (entityManager != null) {
            return entityManager.unwrap(Session.class);
        }
        throw new IllegalStateException("无法获取Session，请确保已正确注入SessionFactory或EntityManager");
    }
    
    /**
     * 保存实体
     */
    public T save(T entity) {
        getSession().saveOrUpdate(entity);
        return entity;
    }
    
    /**
     * 保存或更新实体
     */
    public T saveOrUpdate(T entity) {
        getSession().saveOrUpdate(entity);
        return entity;
    }
    
    /**
     * 根据ID查找实体
     */
    public Optional<T> findById(ID id) {
        T entity = getSession().get(entityClass, id);
        return Optional.ofNullable(entity);
    }
    
    /**
     * 查找所有实体
     */
    public List<T> findAll() {
        return getSession()
            .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
            .list();
    }
    
    /**
     * 根据ID删除实体
     */
    public void deleteById(ID id) {
        Optional<T> entityOpt = findById(id);
        if (entityOpt.isPresent()) {
            delete(entityOpt.get());
        }
    }
    
    /**
     * 删除实体
     */
    public void delete(T entity) {
        getSession().delete(entity);
    }
    
    /**
     * 统计实体数量
     */
    public long count() {
        Long result = getSession()
            .createQuery("SELECT COUNT(*) FROM " + entityClass.getSimpleName(), Long.class)
            .uniqueResult();
        return result != null ? result : 0L;
    }
    
    /**
     * 判断实体是否存在
     */
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
    
    /**
     * 刷新实体
     */
    public void refresh(T entity) {
        getSession().refresh(entity);
    }
    
    /**
     * 合并实体
     */
    @SuppressWarnings("unchecked")
    public T merge(T entity) {
        return (T) getSession().merge(entity);
    }
    
    /**
     * 创建HQL查询
     */
    protected Query<T> createQuery(String hql) {
        return getSession().createQuery(hql, entityClass);
    }
    
    /**
     * 创建原生SQL查询
     */
    protected Query<?> createNativeQuery(String sql) {
        return getSession().createNativeQuery(sql);
    }
    
    /**
     * 获取查询助手
     */
    protected HibernateQueryHelper getQueryHelper() {
        return new HibernateQueryHelper(getSession());
    }
    
    /**
     * 获取批量操作助手
     */
    protected BatchOperationHelper getBatchHelper() {
        return new BatchOperationHelper(getSession());
    }
    
    /**
     * 获取批量操作助手（指定批量大小）
     */
    protected BatchOperationHelper getBatchHelper(int batchSize) {
        return new BatchOperationHelper(getSession(), batchSize);
    }
    
    /**
     * 分页查询
     */
    public PageQuery<T> findAll(int page, int size) {
        return getQueryHelper().pageQuery("FROM " + entityClass.getSimpleName(), entityClass, page, size);
    }
    
    /**
     * 分页查询（带条件）
     */
    public PageQuery<T> findAll(String whereClause, Map<String, Object> parameters, int page, int size) {
        String hql = "FROM " + entityClass.getSimpleName() + " WHERE " + whereClause;
        return getQueryHelper().pageQuery(hql, entityClass, parameters, page, size);
    }
    
    /**
     * 使用动态查询构建器进行查询
     */
    public List<T> findByBuilder(DynamicQueryBuilder builder) {
        String hql = "SELECT e FROM " + entityClass.getSimpleName() + " e " + builder.build();
        Query<T> query = createQuery(hql);
        builder.applyParameters(query);
        return query.list();
    }
    
    /**
     * 使用动态查询构建器进行分页查询
     */
    public PageQuery<T> findPageByBuilder(DynamicQueryBuilder builder, int page, int size) {
        String hql = "SELECT e FROM " + entityClass.getSimpleName() + " e " + builder.build();
        return getQueryHelper().pageQuery(hql, entityClass, builder.getParameters(), page, size);
    }
    
    /**
     * 批量保存
     */
    public void saveAll(Collection<T> entities) {
        getBatchHelper().batchSave(entities);
    }
    
    /**
     * 批量更新
     */
    public void updateAll(Collection<T> entities) {
        getBatchHelper().batchUpdate(entities);
    }
    
    /**
     * 批量保存或更新
     */
    public void saveOrUpdateAll(Collection<T> entities) {
        getBatchHelper().batchSaveOrUpdate(entities);
    }
    
    /**
     * 批量删除
     */
    public void deleteAll(Collection<T> entities) {
        getBatchHelper().batchDelete(entities);
    }
}

