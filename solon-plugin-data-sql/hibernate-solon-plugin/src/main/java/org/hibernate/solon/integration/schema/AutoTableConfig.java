package org.hibernate.solon.integration.schema;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Bean;
import org.noear.solon.core.Props;

/**
 * 自动表配置类
 * 
 * <p>提供自动表创建相关的配置和增强功能</p>
 * 
 * @author noear
 * @since 3.4
 */
@Configuration
public class AutoTableConfig {
    
    /**
     * 配置自动表创建相关属性
     */
    @Bean
    public void configureAutoTable() {
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return;
        }
        
        // 配置表名策略
        String namingStrategy = jpaProps.get("properties.hibernate.physical_naming_strategy");
        if (namingStrategy != null && !namingStrategy.isEmpty()) {
            Solon.cfg().put("jpa.properties.hibernate.physical_naming_strategy", namingStrategy);
        }
        
        // 配置表注释支持（MySQL）
        boolean enableTableComments = jpaProps.getBool("properties.hibernate.enable_table_comments", true);
        if (enableTableComments) {
            // MySQL支持表注释
            String dialect = jpaProps.get("properties.hibernate.dialect", "");
            if (dialect != null && dialect.contains("MySQL")) {
                Solon.cfg().put("jpa.properties.hibernate.globally_quoted_identifiers", "false");
            }
        }
        
        // 配置自动表创建时的详细日志
        boolean enableDdlLog = jpaProps.getBool("properties.hibernate.ddl_log", true);
        if (enableDdlLog) {
            Solon.cfg().put("jpa.properties.hibernate.show_sql", "true");
            Solon.cfg().put("jpa.properties.hibernate.format_sql", "true");
        }
    }
    
    /**
     * 获取自动表创建配置
     */
    public static AutoTableSettings getSettings() {
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return new AutoTableSettings();
        }
        
        AutoTableSettings settings = new AutoTableSettings();
        settings.setDdlAuto(jpaProps.get("properties.hibernate.hbm2ddl.auto", "none"));
        settings.setEnableTableComments(jpaProps.getBool("properties.hibernate.enable_table_comments", true));
        settings.setEnableDdlLog(jpaProps.getBool("properties.hibernate.ddl_log", true));
        settings.setEnableSchemaValidation(jpaProps.getBool("properties.hibernate.enable_schema_validation", true));
        settings.setSkipOnError(jpaProps.getBool("properties.hibernate.ddl_skip_on_error", false));
        
        return settings;
    }
    
    /**
     * 自动表设置
     */
    public static class AutoTableSettings {
        private String ddlAuto = "none";
        private boolean enableTableComments = true;
        private boolean enableDdlLog = true;
        private boolean enableSchemaValidation = true;
        private boolean skipOnError = false;
        
        public String getDdlAuto() {
            return ddlAuto;
        }
        
        public void setDdlAuto(String ddlAuto) {
            this.ddlAuto = ddlAuto;
        }
        
        public boolean isEnableTableComments() {
            return enableTableComments;
        }
        
        public void setEnableTableComments(boolean enableTableComments) {
            this.enableTableComments = enableTableComments;
        }
        
        public boolean isEnableDdlLog() {
            return enableDdlLog;
        }
        
        public void setEnableDdlLog(boolean enableDdlLog) {
            this.enableDdlLog = enableDdlLog;
        }
        
        public boolean isEnableSchemaValidation() {
            return enableSchemaValidation;
        }
        
        public void setEnableSchemaValidation(boolean enableSchemaValidation) {
            this.enableSchemaValidation = enableSchemaValidation;
        }
        
        public boolean isSkipOnError() {
            return skipOnError;
        }
        
        public void setSkipOnError(boolean skipOnError) {
            this.skipOnError = skipOnError;
        }
    }
}

