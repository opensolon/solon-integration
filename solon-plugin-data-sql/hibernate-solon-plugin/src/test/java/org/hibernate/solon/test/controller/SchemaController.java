package org.hibernate.solon.test.controller;

import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.hibernate.solon.integration.schema.SchemaManager;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import javax.sql.DataSource;

/**
 * Schema管理控制器（测试用）
 * 
 * @author noear
 * @since 3.4
 */
@Controller
@Mapping("/api/schema")
public class SchemaController {
    
    @Inject
    private DataSource dataSource;
    
    /**
     * 生成DDL脚本到文件
     */
    @Mapping("/generate")
    public String generateDdl(String outputFile) {
        try {
            // 获取默认的HibernateAdapter
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return "未找到Hibernate适配器";
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            schemaManager.generateDdlToFile(outputFile, true);
            
            return "DDL脚本已生成到: " + outputFile;
        } catch (Exception e) {
            return "生成DDL失败: " + e.getMessage();
        }
    }
    
    /**
     * 获取DDL字符串
     */
    @Mapping("/ddl")
    public String getDdlString() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return "未找到Hibernate适配器";
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            return schemaManager.generateDdlString(true);
        } catch (Exception e) {
            return "生成DDL失败: " + e.getMessage();
        }
    }
    
    /**
     * 创建Schema
     */
    @Mapping("/create")
    public String createSchema(boolean drop) {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return "未找到Hibernate适配器";
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            schemaManager.createSchema(drop);
            
            return "Schema创建成功";
        } catch (Exception e) {
            return "创建Schema失败: " + e.getMessage();
        }
    }
    
    /**
     * 更新Schema
     */
    @Mapping("/update")
    public String updateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return "未找到Hibernate适配器";
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            schemaManager.updateSchema();
            
            return "Schema更新成功";
        } catch (Exception e) {
            return "更新Schema失败: " + e.getMessage();
        }
    }
    
    /**
     * 验证Schema
     */
    @Mapping("/validate")
    public String validateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return "未找到Hibernate适配器";
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
            
            return result.isValid() ? 
                "验证通过: " + result.getMessage() : 
                "验证失败: " + result.getMessage();
        } catch (Exception e) {
            return "验证Schema失败: " + e.getMessage();
        }
    }
}

