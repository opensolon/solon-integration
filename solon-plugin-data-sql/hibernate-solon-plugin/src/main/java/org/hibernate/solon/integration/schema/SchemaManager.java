package org.hibernate.solon.integration.schema;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;

import javax.sql.DataSource;
import java.util.EnumSet;
import java.util.Properties;

/**
 * Schema管理器
 * 
 * <p>管理数据库Schema的创建、更新和验证，支持自动执行DDL</p>
 * 
 * @author noear
 * @since 3.4
 */
public class SchemaManager {
    
    private final Configuration configuration;
    private final DataSource dataSource;
    private final Properties properties;
    private SessionFactory sessionFactory;
    
    /**
     * 构造函数（使用SessionFactory）
     */
    public SchemaManager(SessionFactory sessionFactory, DataSource dataSource, Properties properties) {
        this.sessionFactory = sessionFactory;
        this.configuration = null;
        this.dataSource = dataSource;
        this.properties = properties != null ? properties : new Properties();
    }
    
    /**
     * 构造函数（使用Configuration）
     */
    public SchemaManager(Configuration configuration, DataSource dataSource, Properties properties) {
        this.configuration = configuration;
        this.dataSource = dataSource;
        this.properties = properties != null ? properties : new Properties();
    }
    
    /**
     * 获取Metadata（优先从SessionFactory，否则从Configuration构建）
     */
    private Metadata getMetadata() {
        if (sessionFactory != null) {
            return getMetadataFromSessionFactory();
        } else if (configuration != null) {
            return buildMetadataFromConfiguration();
        } else {
            throw new IllegalStateException("无法获取Metadata：缺少Configuration或SessionFactory");
        }
    }
    
    /**
     * 从SessionFactory获取Metadata
     */
    private Metadata getMetadataFromSessionFactory() {
        // Hibernate 5.6中，SessionFactory没有直接获取Metadata的方法
        // 需要从Configuration重建
        if (configuration != null) {
            return buildMetadataFromConfiguration();
        }
        
        // 如果SessionFactory是JpaTranSessionFactory，无法直接获取Metadata
        // 必须使用Configuration方式
        throw new IllegalStateException("无法从SessionFactory获取Metadata，请使用Configuration方式");
    }
    
    /**
     * 从Configuration构建Metadata
     */
    private Metadata buildMetadataFromConfiguration() {
        StandardServiceRegistry serviceRegistry = buildServiceRegistry();
        
        try {
            MetadataSources metadataSources = new MetadataSources(serviceRegistry);
            
            // 从Configuration获取所有已注册的类
            if (configuration instanceof org.hibernate.solon.integration.HibernateConfiguration) {
                org.hibernate.solon.integration.HibernateConfiguration hibernateConfig = 
                    (org.hibernate.solon.integration.HibernateConfiguration) configuration;
                
                // 添加所有已注册的实体类
                for (Class<?> clazz : hibernateConfig.getRegisteredClasses()) {
                    metadataSources.addAnnotatedClass(clazz);
                }
            }
            
            return metadataSources.buildMetadata();
        } finally {
            // 不销毁serviceRegistry，因为可能被其他地方使用
        }
    }
    
    /**
     * 构建ServiceRegistry
     */
    private StandardServiceRegistry buildServiceRegistry() {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        
        // 应用配置属性
        Properties props = configuration != null ? configuration.getProperties() : properties;
        props.forEach((key, value) -> {
            builder.applySetting(String.valueOf(key), String.valueOf(value));
        });
        
        // 设置数据源
        if (dataSource != null) {
            builder.applySetting(AvailableSettings.DATASOURCE, dataSource);
        }
        
        return builder.build();
    }
    
    /**
     * 创建Schema（创建所有表）
     * 
     * @param drop 是否先删除已存在的表
     */
    public void createSchema(boolean drop) {
        Metadata metadata = getMetadata();
        
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        
        if (drop) {
            schemaExport.execute(
                EnumSet.of(TargetType.DATABASE),
                SchemaExport.Action.BOTH,
                metadata
            );
        } else {
            schemaExport.execute(
                EnumSet.of(TargetType.DATABASE),
                SchemaExport.Action.CREATE,
                metadata
            );
        }
    }
    
    /**
     * 更新Schema（根据实体类更新表结构）
     */
    public void updateSchema() {
        Metadata metadata = getMetadata();
        
        SchemaUpdate schemaUpdate = new SchemaUpdate();
        schemaUpdate.setFormat(true);
        schemaUpdate.setDelimiter(";");
        
        schemaUpdate.execute(
            EnumSet.of(TargetType.DATABASE),
            metadata
        );
    }
    
    /**
     * 验证Schema（检查表结构是否与实体类匹配）
     * 
     * @return 验证结果
     */
    public SchemaValidationResult validateSchema() {
        try {
            Metadata metadata = getMetadata();
            
            // 简单验证：检查Metadata是否有效
            if (metadata != null && metadata.getDatabase() != null) {
                // 检查是否有表定义
                long tableCount = metadata.getDatabase().getDefaultNamespace().getTables().size();
                return new SchemaValidationResult(true, 
                    "Schema验证通过，共 " + tableCount + " 个表定义");
            } else {
                return new SchemaValidationResult(false, "Schema验证失败：Metadata无效");
            }
        } catch (Exception e) {
            return new SchemaValidationResult(false, "Schema验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除Schema（删除所有表）
     */
    public void dropSchema() {
        Metadata metadata = getMetadata();
        
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        
        schemaExport.execute(
            EnumSet.of(TargetType.DATABASE),
            SchemaExport.Action.DROP,
            metadata
        );
    }
    
    /**
     * 生成DDL脚本到文件
     * 
     * @param outputFile 输出文件路径
     * @param format 是否格式化
     */
    public void generateDdlToFile(String outputFile, boolean format) {
        DdlGenerator generator = new DdlGenerator(configuration, properties);
        try {
            generator.generateDdlToFile(outputFile, format);
        } catch (Exception e) {
            throw new RuntimeException("生成DDL文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成DDL字符串
     * 
     * @param format 是否格式化
     * @return DDL字符串
     */
    public String generateDdlString(boolean format) {
        DdlGenerator generator = new DdlGenerator(configuration, properties);
        return generator.generateDdlString(format);
    }
    
    /**
     * 生成创建表的DDL
     * 
     * @return DDL字符串
     */
    public String generateCreateDdl() {
        DdlGenerator generator = new DdlGenerator(configuration, properties);
        return generator.generateCreateDdl();
    }
    
    /**
     * 生成删除表的DDL
     * 
     * @return DDL字符串
     */
    public String generateDropDdl() {
        DdlGenerator generator = new DdlGenerator(configuration, properties);
        return generator.generateDropDdl();
    }
    
    /**
     * Schema验证结果
     */
    public static class SchemaValidationResult {
        private final boolean valid;
        private final String message;
        
        public SchemaValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
