package org.hibernate.solon.integration.batch;

import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import java.util.Collection;
import java.util.List;

/**
 * 批量操作助手类
 * 
 * <p>提供批量插入、更新、删除等操作，提升性能</p>
 * 
 * @author noear
 * @since 3.4
 */
public class BatchOperationHelper {
    
    private final Session session;
    private final int batchSize;
    
    /**
     * 构造函数
     * 
     * @param session Hibernate Session
     * @param batchSize 批量大小（默认50）
     */
    public BatchOperationHelper(Session session, int batchSize) {
        this.session = session;
        this.batchSize = batchSize > 0 ? batchSize : 50;
    }
    
    /**
     * 构造函数（使用默认批量大小50）
     */
    public BatchOperationHelper(Session session) {
        this(session, 50);
    }
    
    /**
     * 批量保存实体
     * 
     * @param entities 实体集合
     * @param <T> 实体类型
     */
    public <T> void batchSave(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        int count = 0;
        for (T entity : entities) {
            session.save(entity);
            count++;
            
            if (count % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        
        session.flush();
        session.clear();
    }
    
    /**
     * 批量更新实体
     * 
     * @param entities 实体集合
     * @param <T> 实体类型
     */
    public <T> void batchUpdate(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        int count = 0;
        for (T entity : entities) {
            session.update(entity);
            count++;
            
            if (count % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        
        session.flush();
        session.clear();
    }
    
    /**
     * 批量保存或更新实体
     * 
     * @param entities 实体集合
     * @param <T> 实体类型
     */
    public <T> void batchSaveOrUpdate(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        int count = 0;
        for (T entity : entities) {
            session.saveOrUpdate(entity);
            count++;
            
            if (count % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        
        session.flush();
        session.clear();
    }
    
    /**
     * 批量删除实体
     * 
     * @param entities 实体集合
     * @param <T> 实体类型
     */
    public <T> void batchDelete(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        int count = 0;
        for (T entity : entities) {
            session.delete(entity);
            count++;
            
            if (count % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        
        session.flush();
        session.clear();
    }
    
    /**
     * 使用StatelessSession进行批量插入（性能更好，但不支持级联）
     * 
     * @param entities 实体集合
     * @param <T> 实体类型
     */
    public <T> void batchInsertWithStateless(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        StatelessSession statelessSession = session.getSessionFactory().openStatelessSession();
        Transaction transaction = statelessSession.beginTransaction();
        
        try {
            int count = 0;
            for (T entity : entities) {
                statelessSession.insert(entity);
                count++;
                
                if (count % batchSize == 0) {
                    transaction.commit();
                    transaction = statelessSession.beginTransaction();
                }
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            statelessSession.close();
        }
    }
    
    /**
     * 批量执行HQL更新
     * 
     * @param hql HQL更新语句
     * @param parameters 参数列表（每个参数对应一次更新）
     * @return 总影响行数
     */
    public int batchExecuteUpdate(String hql, List<Object[]> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return 0;
        }
        
        int totalAffected = 0;
        int count = 0;
        
        for (Object[] params : parameters) {
            org.hibernate.query.Query<?> query = session.createQuery(hql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    query.setParameter(i, params[i]);
                }
            }
            
            totalAffected += query.executeUpdate();
            count++;
            
            if (count % batchSize == 0) {
                session.flush();
                session.clear();
            }
        }
        
        session.flush();
        session.clear();
        
        return totalAffected;
    }
    
    /**
     * 获取批量大小
     */
    public int getBatchSize() {
        return batchSize;
    }
}

