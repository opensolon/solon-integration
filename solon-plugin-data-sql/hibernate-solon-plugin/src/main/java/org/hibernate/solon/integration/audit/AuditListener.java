package org.hibernate.solon.integration.audit;

import org.hibernate.event.spi.*;
import org.hibernate.solon.annotation.CreatedDate;
import org.hibernate.solon.annotation.LastModifiedDate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 审计监听器
 * 
 * <p>自动处理@CreatedDate和@LastModifiedDate注解</p>
 * 
 * @author noear
 * @since 3.4
 */
public class AuditListener implements PreInsertEventListener, PreUpdateEventListener {
    
    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();
        setCreatedDate(entity, event.getState(), event.getPersister().getPropertyNames());
        setLastModifiedDate(entity, event.getState(), event.getPersister().getPropertyNames());
        return false;
    }
    
    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Object entity = event.getEntity();
        setLastModifiedDate(entity, event.getState(), event.getPersister().getPropertyNames());
        return false;
    }
    
    /**
     * 设置创建时间
     */
    private void setCreatedDate(Object entity, Object[] state, String[] propertyNames) {
        Field[] fields = entity.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(CreatedDate.class)) {
                try {
                    field.setAccessible(true);
                    Object currentValue = field.get(entity);
                    
                    // 如果已经有值，不覆盖
                    if (currentValue != null) {
                        continue;
                    }
                    
                    // 设置当前时间
                    Object timestamp = getCurrentTimestamp(field.getType());
                    field.set(entity, timestamp);
                    
                    // 更新state数组
                    for (int i = 0; i < propertyNames.length; i++) {
                        if (propertyNames[i].equals(field.getName())) {
                            state[i] = timestamp;
                            break;
                        }
                    }
                } catch (IllegalAccessException e) {
                    // 忽略无法访问的字段
                }
            }
        }
    }
    
    /**
     * 设置最后修改时间
     */
    private void setLastModifiedDate(Object entity, Object[] state, String[] propertyNames) {
        Field[] fields = entity.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(LastModifiedDate.class)) {
                try {
                    field.setAccessible(true);
                    
                    // 设置当前时间
                    Object timestamp = getCurrentTimestamp(field.getType());
                    field.set(entity, timestamp);
                    
                    // 更新state数组
                    for (int i = 0; i < propertyNames.length; i++) {
                        if (propertyNames[i].equals(field.getName())) {
                            state[i] = timestamp;
                            break;
                        }
                    }
                } catch (IllegalAccessException e) {
                    // 忽略无法访问的字段
                }
            }
        }
    }
    
    /**
     * 获取当前时间戳（根据字段类型）
     */
    private Object getCurrentTimestamp(Class<?> type) {
        if (type == LocalDateTime.class) {
            return LocalDateTime.now();
        } else if (type == Date.class) {
            return new Date();
        } else if (type == Long.class || type == long.class) {
            return System.currentTimeMillis();
        } else if (type == java.sql.Timestamp.class) {
            return new java.sql.Timestamp(System.currentTimeMillis());
        } else {
            return new Date();
        }
    }
}

