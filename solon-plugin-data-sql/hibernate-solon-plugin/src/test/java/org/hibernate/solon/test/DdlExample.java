package org.hibernate.solon.test;

import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.hibernate.solon.integration.schema.DdlGenerator;
import org.hibernate.solon.integration.schema.SchemaManager;
import org.noear.solon.annotation.Component;

/**
 * DDL功能使用示例
 * 
 * <p>演示如何使用Hibernate DDL功能</p>
 * 
 * @author noear
 * @since 3.4
 */
@Component
public class DdlExample {
    
    /**
     * 示例1：生成DDL脚本到文件
     * 
     * 用途：生成SQL脚本，用于数据库迁移或版本控制
     */
    public void example1_GenerateDdlToFile() {
        try {
            // 1. 获取Hibernate适配器
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            
            if (adapter == null) {
                System.out.println("未找到Hibernate适配器");
                return;
            }
            
            // 2. 获取DDL生成器
            DdlGenerator generator = adapter.getDdlGenerator();
            
            // 3. 生成DDL到文件
            String outputFile = "target/schema.sql";
            generator.generateDdlToFile(outputFile, true);
            
            System.out.println("✅ DDL脚本已生成到: " + outputFile);
            System.out.println("   可以用于数据库迁移或版本控制");
            
        } catch (Exception e) {
            System.err.println("❌ 生成DDL失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 示例2：生成DDL字符串并打印
     * 
     * 用途：查看生成的SQL语句，用于调试
     */
    public void example2_GenerateDdlString() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            DdlGenerator generator = adapter.getDdlGenerator();
            
            // 生成DDL字符串
            String ddl = generator.generateDdlString(true);
            
            System.out.println("✅ 生成的DDL SQL:");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println(ddl);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (Exception e) {
            System.err.println("❌ 生成DDL失败: " + e.getMessage());
        }
    }
    
    /**
     * 示例3：生成创建表的DDL
     * 
     * 用途：只生成CREATE TABLE语句
     */
    public void example3_GenerateCreateDdl() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            DdlGenerator generator = adapter.getDdlGenerator();
            
            // 生成创建表的DDL
            String createDdl = generator.generateCreateDdl();
            
            System.out.println("✅ 创建表的DDL:");
            System.out.println(createDdl);
            
        } catch (Exception e) {
            System.err.println("❌ 生成创建DDL失败: " + e.getMessage());
        }
    }
    
    /**
     * 示例4：生成删除表的DDL
     * 
     * 用途：生成DROP TABLE语句（用于清理）
     */
    public void example4_GenerateDropDdl() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            DdlGenerator generator = adapter.getDdlGenerator();
            
            // 生成删除表的DDL
            String dropDdl = generator.generateDropDdl();
            
            System.out.println("✅ 删除表的DDL:");
            System.out.println(dropDdl);
            System.out.println("⚠️  警告：执行此DDL会删除所有表和数据！");
            
        } catch (Exception e) {
            System.err.println("❌ 生成删除DDL失败: " + e.getMessage());
        }
    }
    
    /**
     * 示例5：手动创建Schema（创建所有表）
     * 
     * 用途：手动执行建表操作
     * 
     * ⚠️ 警告：会创建表，如果表已存在可能报错
     */
    public void example5_CreateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            // 创建Schema（不删除已存在的表）
            schemaManager.createSchema(false);
            
            System.out.println("✅ Schema创建成功");
            System.out.println("   所有表已创建到数据库");
            
        } catch (Exception e) {
            System.err.println("❌ 创建Schema失败: " + e.getMessage());
            System.err.println("   可能原因：表已存在，或数据库连接失败");
        }
    }
    
    /**
     * 示例6：更新Schema（更新表结构）
     * 
     * 用途：添加缺失的列和约束，不删除现有列
     */
    public void example6_UpdateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            // 更新Schema
            schemaManager.updateSchema();
            
            System.out.println("✅ Schema更新成功");
            System.out.println("   已添加缺失的列和约束");
            System.out.println("   注意：不会删除已存在的列");
            
        } catch (Exception e) {
            System.err.println("❌ 更新Schema失败: " + e.getMessage());
        }
    }
    
    /**
     * 示例7：验证Schema
     * 
     * 用途：检查数据库表结构是否与实体类匹配
     */
    public void example7_ValidateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            // 验证Schema
            SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
            
            if (result.isValid()) {
                System.out.println("✅ Schema验证通过");
                System.out.println("   消息: " + result.getMessage());
            } else {
                System.out.println("❌ Schema验证失败");
                System.out.println("   消息: " + result.getMessage());
                System.out.println("   请检查数据库表结构是否与实体类匹配");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 验证Schema失败: " + e.getMessage());
        }
    }
    
    /**
     * 示例8：删除Schema（删除所有表）
     * 
     * ⚠️ 警告：会删除所有表和数据，请谨慎使用！
     */
    public void example8_DropSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            // 删除Schema
            schemaManager.dropSchema();
            
            System.out.println("✅ Schema删除成功");
            System.out.println("⚠️  警告：所有表和数据已被删除！");
            
        } catch (Exception e) {
            System.err.println("❌ 删除Schema失败: " + e.getMessage());
        }
    }
    
    /**
     * 示例9：完整工作流程
     * 
     * 演示从生成DDL到执行DDL的完整流程
     */
    public void example9_CompleteWorkflow() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            DdlGenerator generator = adapter.getDdlGenerator();
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("DDL完整工作流程示例");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 步骤1：生成DDL脚本
            System.out.println("\n📝 步骤1：生成DDL脚本");
            String ddl = generator.generateDdlString(true);
            System.out.println("生成的DDL长度: " + ddl.length() + " 字符");
            
            // 步骤2：保存到文件
            System.out.println("\n💾 步骤2：保存DDL到文件");
            generator.generateDdlToFile("target/schema.sql", true);
            System.out.println("已保存到: target/schema.sql");
            
            // 步骤3：验证Schema
            System.out.println("\n🔍 步骤3：验证Schema");
            SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
            if (result.isValid()) {
                System.out.println("✅ 验证通过: " + result.getMessage());
            } else {
                System.out.println("❌ 验证失败: " + result.getMessage());
            }
            
            // 步骤4：更新Schema（如果需要）
            System.out.println("\n🔄 步骤4：更新Schema（如果需要）");
            System.out.println("执行 updateSchema()...");
            // schemaManager.updateSchema(); // 取消注释以执行
            System.out.println("✅ Schema更新完成");
            
            System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("工作流程完成！");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (Exception e) {
            System.err.println("❌ 工作流程失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

