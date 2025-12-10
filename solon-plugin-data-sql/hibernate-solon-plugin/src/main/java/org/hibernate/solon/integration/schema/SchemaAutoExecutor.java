package org.hibernate.solon.integration.schema;

import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.Props;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema自动执行器
 * 
 * <p>根据hbm2ddl.auto配置自动执行DDL操作</p>
 * 
 * @author noear
 * @since 3.4
 */
@Configuration
public class SchemaAutoExecutor implements EventListener<AppLoadEndEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(SchemaAutoExecutor.class);
    
    /**
     * 配置Schema自动执行
     */
    @Bean
    public void configureAutoDdl() {
        // 注册应用加载完成事件监听
        Solon.app().onEvent(AppLoadEndEvent.class, this);
    }
    
    @Override
    public void onEvent(AppLoadEndEvent event) {
        // 遍历所有Hibernate适配器，执行自动DDL
        HibernateAdapterManager.getAll().forEach((name, adapter) -> {
            executeAutoDdlForAdapter(adapter, name);
        });
    }
    
    /**
     * 为指定的适配器执行自动DDL
     */
    private void executeAutoDdlForAdapter(HibernateAdapter adapter, String adapterName) {
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return;
        }
        
        // 获取该适配器的DDL配置
        String ddlAuto;
        if (adapterName != null && !adapterName.isEmpty()) {
            ddlAuto = jpaProps.get("properties.hibernate.hbm2ddl.auto", 
                jpaProps.get(adapterName + ".properties.hibernate.hbm2ddl.auto", ""));
        } else {
            ddlAuto = jpaProps.get("properties.hibernate.hbm2ddl.auto", "");
        }
        
        if (ddlAuto == null || ddlAuto.isEmpty() || "none".equalsIgnoreCase(ddlAuto)) {
            log.debug("Hibernate自动DDL未启用 (adapter: {})", adapterName);
            return;
        }
        
        // 获取配置
        AutoTableConfig.AutoTableSettings settings = AutoTableConfig.getSettings();
        boolean skipOnError = settings.isSkipOnError();
        
        try {
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("开始执行Hibernate自动DDL (adapter: {}, strategy: {})", adapterName, ddlAuto);
            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            long startTime = System.currentTimeMillis();
            
            switch (ddlAuto.toLowerCase()) {
                case "create":
                    executeCreate(schemaManager, adapterName, startTime);
                    break;
                case "create-drop":
                    executeCreateDrop(schemaManager, adapterName, startTime);
                    break;
                case "update":
                    executeUpdate(schemaManager, adapterName, startTime);
                    break;
                case "validate":
                    executeValidate(schemaManager, adapterName, startTime);
                    break;
                default:
                    log.warn("未知的DDL策略: {} (adapter: {})", ddlAuto, adapterName);
                    break;
            }
            
        } catch (Exception e) {
            String errorMsg = "执行自动DDL失败 (adapter: " + adapterName + ", strategy: " + ddlAuto + "): " + e.getMessage();
            
            if (skipOnError) {
                log.warn(errorMsg);
                log.warn("由于配置了skip_on_error=true，继续执行...");
            } else {
                log.error(errorMsg, e);
                throw new RuntimeException(errorMsg, e);
            }
        }
    }
    
    /**
     * 执行create策略
     */
    private void executeCreate(SchemaManager schemaManager, String adapterName, long startTime) {
        log.info("执行策略: CREATE - 创建所有表");
        schemaManager.createSchema(false);
        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Hibernate自动创建Schema完成 (adapter: {}, 耗时: {}ms)", adapterName, duration);
    }
    
    /**
     * 执行create-drop策略
     */
    private void executeCreateDrop(SchemaManager schemaManager, String adapterName, long startTime) {
        log.info("执行策略: CREATE-DROP - 创建表（关闭时删除）");
        schemaManager.createSchema(false);
        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Hibernate自动创建Schema完成 (adapter: {}, 耗时: {}ms)", adapterName, duration);
        log.info("⚠️  注意: 应用关闭时将自动删除所有表");
        // 注意：create-drop需要在应用关闭时执行drop，这里只执行create
    }
    
    /**
     * 执行update策略
     */
    private void executeUpdate(SchemaManager schemaManager, String adapterName, long startTime) {
        log.info("执行策略: UPDATE - 更新表结构");
        schemaManager.updateSchema();
        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ Hibernate自动更新Schema完成 (adapter: {}, 耗时: {}ms)", adapterName, duration);
    }
    
    /**
     * 执行validate策略
     */
    private void executeValidate(SchemaManager schemaManager, String adapterName, long startTime) {
        log.info("执行策略: VALIDATE - 验证表结构");
        SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
        long duration = System.currentTimeMillis() - startTime;
        
        if (result.isValid()) {
            log.info("✅ Hibernate Schema验证通过 (adapter: {}, 耗时: {}ms): {}", 
                adapterName, duration, result.getMessage());
        } else {
            log.error("❌ Hibernate Schema验证失败 (adapter: {}, 耗时: {}ms): {}", 
                adapterName, duration, result.getMessage());
            throw new RuntimeException("Schema验证失败: " + result.getMessage());
        }
    }
}

