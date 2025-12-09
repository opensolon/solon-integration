package org.hibernate.solon.integration.schema;

import org.hibernate.cfg.AvailableSettings;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.Props;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;

/**
 * Schema配置类
 * 
 * <p>自动管理数据库Schema，支持DDL自动执行</p>
 * 
 * @author noear
 * @since 3.4
 */
@Configuration
public class SchemaConfiguration implements EventListener<AppLoadEndEvent> {
    
    /**
     * 配置Schema管理
     */
    @Bean
    public void configureSchema() {
        // 注册应用加载完成事件监听
        Solon.app().onEvent(AppLoadEndEvent.class, this);
    }
    
    @Override
    public void onEvent(AppLoadEndEvent event) {
        // 在应用加载完成后，根据配置执行Schema操作
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return;
        }
        
        // 获取DDL策略
        String ddlAuto = jpaProps.get("properties.hibernate.hbm2ddl.auto", "");
        
        if (Utils.isEmpty(ddlAuto)) {
            return;
        }
        
        // 根据策略执行相应操作
        // 注意：这里只是配置，实际执行需要在HibernateAdapter中处理
        // 因为需要SessionFactory和Configuration
    }
    
    /**
     * 获取DDL策略
     */
    public static DdlStrategy getDdlStrategy() {
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return DdlStrategy.NONE;
        }
        
        String ddlAuto = jpaProps.get("properties.hibernate.hbm2ddl.auto", "none");
        
        switch (ddlAuto.toLowerCase()) {
            case "create":
                return DdlStrategy.CREATE;
            case "create-drop":
                return DdlStrategy.CREATE_DROP;
            case "update":
                return DdlStrategy.UPDATE;
            case "validate":
                return DdlStrategy.VALIDATE;
            case "none":
            default:
                return DdlStrategy.NONE;
        }
    }
    
    /**
     * DDL策略枚举
     */
    public enum DdlStrategy {
        /**
         * 不执行任何操作
         */
        NONE,
        
        /**
         * 启动时创建表，关闭时不删除
         */
        CREATE,
        
        /**
         * 启动时创建表，关闭时删除表
         */
        CREATE_DROP,
        
        /**
         * 启动时更新表结构（添加缺失的列和约束）
         */
        UPDATE,
        
        /**
         * 启动时验证表结构，不修改数据库
         */
        VALIDATE
    }
}

