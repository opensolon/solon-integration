package org.hibernate.solon.integration.schema;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.Properties;

/**
 * DDL生成器
 * 
 * <p>用于生成数据库Schema的DDL语句，支持从实体类自动生成建表SQL</p>
 * 
 * @author noear
 * @since 3.4
 */
public class DdlGenerator {
    
    private final Configuration configuration;
    private final Properties properties;
    private SessionFactory sessionFactory;
    
    /**
     * 构造函数（使用SessionFactory）
     */
    public DdlGenerator(SessionFactory sessionFactory, Properties properties) {
        this.sessionFactory = sessionFactory;
        this.configuration = null;
        this.properties = properties != null ? properties : new Properties();
    }
    
    /**
     * 构造函数（使用Configuration）
     */
    public DdlGenerator(Configuration configuration, Properties properties) {
        this.configuration = configuration;
        this.properties = properties != null ? properties : new Properties();
    }
    
    /**
     * 获取Metadata
     */
    private Metadata getMetadata() {
        if (sessionFactory != null) {
            // 从SessionFactory获取Metadata（Hibernate 5.6方式）
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
        // 需要通过Configuration重建Metadata
        // 注意：这需要Configuration保存了实体类信息
        if (configuration != null) {
            return buildMetadataFromConfiguration();
        }
        
        // 如果SessionFactory是JpaTranSessionFactory，尝试获取真实的SessionFactory
        if (sessionFactory instanceof org.hibernate.solon.integration.JpaTranSessionFactory) {
            // 无法直接获取，需要从Configuration重建
            throw new IllegalStateException("无法从SessionFactory获取Metadata，请使用Configuration方式");
        }
        
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
        
        return builder.build();
    }
    
    /**
     * 生成DDL脚本到文件
     * 
     * @param outputFile 输出文件路径
     * @param format 是否格式化SQL
     * @throws IOException IO异常
     */
    public void generateDdlToFile(String outputFile, boolean format) throws IOException {
        generateDdlToFile(outputFile, format, true);
    }
    
    /**
     * 生成DDL脚本到文件
     * 
     * @param outputFile 输出文件路径
     * @param format 是否格式化SQL
     * @param delimiter 是否添加分隔符
     * @throws IOException IO异常
     */
    public void generateDdlToFile(String outputFile, boolean format, boolean delimiter) throws IOException {
        File file = new File(outputFile);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileWriter writer = new FileWriter(file, false)) {
            generateDdl(writer, format, delimiter);
        }
    }
    
    /**
     * 生成DDL脚本到Writer
     * 
     * @param writer 输出Writer
     * @param format 是否格式化SQL
     * @param delimiter 是否添加分隔符
     */
    public void generateDdl(Writer writer, boolean format, boolean delimiter) {
        try {
            String ddl = generateDdlString(format);
            writer.write(ddl);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("写入DDL失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成DDL字符串
     * 
     * @param format 是否格式化
     * @return DDL字符串
     */
    public String generateDdlString(boolean format) {
        Metadata metadata = getMetadata();
        
        // 使用Hibernate 5.6的SchemaExport
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setFormat(format);
        schemaExport.setDelimiter(";");
        
        // 创建临时文件来捕获DDL输出
        try {
            File tempFile = File.createTempFile("hibernate_ddl_", ".sql");
            tempFile.deleteOnExit();
            
            schemaExport.setOutputFile(tempFile.getAbsolutePath());
            schemaExport.create(EnumSet.of(TargetType.SCRIPT), metadata);
            
            // 读取生成的文件内容
            String ddl = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), 
                java.nio.charset.StandardCharsets.UTF_8);
            
            // 删除临时文件
            tempFile.delete();
            
            return ddl;
        } catch (Exception e) {
            throw new RuntimeException("生成DDL失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行DDL（创建表）
     * 
     * @param drop 是否先删除表
     * @param create 是否创建表
     */
    public void executeDdl(boolean drop, boolean create) {
        Metadata metadata = getMetadata();
        
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        
        // 执行DDL
        if (drop && create) {
            schemaExport.execute(
                EnumSet.of(TargetType.DATABASE),
                SchemaExport.Action.BOTH,
                metadata
            );
        } else if (create) {
            schemaExport.execute(
                EnumSet.of(TargetType.DATABASE),
                SchemaExport.Action.CREATE,
                metadata
            );
        } else if (drop) {
            schemaExport.execute(
                EnumSet.of(TargetType.DATABASE),
                SchemaExport.Action.DROP,
                metadata
            );
        }
    }
    
    /**
     * 生成创建表的DDL
     * 
     * @return DDL字符串
     */
    public String generateCreateDdl() {
        return generateDdlString(true);
    }
    
    /**
     * 生成删除表的DDL
     * 
     * @return DDL字符串
     */
    public String generateDropDdl() {
        Metadata metadata = getMetadata();
        
        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        
        try {
            File tempFile = File.createTempFile("hibernate_drop_", ".sql");
            tempFile.deleteOnExit();
            
            schemaExport.setOutputFile(tempFile.getAbsolutePath());
            schemaExport.drop(EnumSet.of(TargetType.SCRIPT), metadata);
            
            String ddl = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), 
                java.nio.charset.StandardCharsets.UTF_8);
            
            tempFile.delete();
            
            return ddl;
        } catch (Exception e) {
            throw new RuntimeException("生成DROP DDL失败: " + e.getMessage(), e);
        }
    }
}
