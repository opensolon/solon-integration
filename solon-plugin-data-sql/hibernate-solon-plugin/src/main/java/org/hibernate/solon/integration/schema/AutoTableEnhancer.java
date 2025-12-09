package org.hibernate.solon.integration.schema;

import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动表增强器
 * 
 * <p>提供自动表创建时的增强功能：
 * - 表结构统计
 * - 变更检测
 * - 详细日志
 * - 错误处理
 * </p>
 * 
 * @author noear
 * @since 3.4
 */
@Component
public class AutoTableEnhancer implements EventListener<AppLoadEndEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(AutoTableEnhancer.class);
    
    @Override
    public void onEvent(AppLoadEndEvent event) {
        // 在所有DDL执行完成后，进行统计和报告
        Solon.app().onEvent(AppLoadEndEvent.class, e -> {
            try {
                reportTableStatistics();
            } catch (Exception ex) {
                log.warn("生成表统计信息失败: " + ex.getMessage());
            }
        });
    }
    
    /**
     * 报告表统计信息
     */
    private void reportTableStatistics() {
        HibernateAdapterManager.getAll().forEach((name, adapter) -> {
            try {
                // 通过SchemaManager验证来获取表信息
                SchemaManager schemaManager = adapter.getSchemaManager();
                SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
                
                if (result.isValid()) {
                    log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    log.info("Hibernate自动表创建统计 (adapter: {})", name);
                    log.info("  {}", result.getMessage());
                    log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                }
            } catch (Exception e) {
                log.warn("获取表统计信息失败 (adapter: {}): {}", name, e.getMessage());
            }
        });
    }
    
    /**
     * 检测表结构变更
     */
    public static TableChangeReport detectTableChanges(HibernateAdapter adapter) {
        TableChangeReport report = new TableChangeReport();
        
        try {
            SchemaManager schemaManager = adapter.getSchemaManager();
            SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
            
            report.setValid(result.isValid());
            report.setMessage(result.getMessage());
            
        } catch (Exception e) {
            report.setValid(false);
            report.setMessage("检测失败: " + e.getMessage());
        }
        
        return report;
    }
    
    /**
     * 表变更报告
     */
    public static class TableChangeReport {
        private boolean valid;
        private String message;
        private List<String> addedTables = new ArrayList<>();
        private List<String> modifiedTables = new ArrayList<>();
        private List<String> removedTables = new ArrayList<>();
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public List<String> getAddedTables() {
            return addedTables;
        }
        
        public void setAddedTables(List<String> addedTables) {
            this.addedTables = addedTables;
        }
        
        public List<String> getModifiedTables() {
            return modifiedTables;
        }
        
        public void setModifiedTables(List<String> modifiedTables) {
            this.modifiedTables = modifiedTables;
        }
        
        public List<String> getRemovedTables() {
            return removedTables;
        }
        
        public void setRemovedTables(List<String> removedTables) {
            this.removedTables = removedTables;
        }
    }
}

