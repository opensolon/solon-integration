package org.hibernate.solon.test;

import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.hibernate.solon.integration.schema.SchemaManager;
import org.junit.jupiter.api.Test;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

import java.io.File;

/**
 * Schema管理器测试类
 * 
 * <p><b>⚠️ 测试前准备：</b></p>
 * <ol>
 *   <li>部分测试方法需要数据库表已存在（如testCreateSchema、testUpdateSchema）</li>
 *   <li>部分测试方法不需要表（如testGenerateDdl、testValidateSchema）</li>
 *   <li>创建表的方式：
 *     <ul>
 *       <li>方式1：配置 hbm2ddl.auto=create 或 update，启动时自动创建</li>
 *       <li>方式2：执行 SQL脚本：src/test/resources/test_schema.sql</li>
 *       <li>方式3：运行 DdlGeneratorTest 生成DDL后手动执行</li>
 *     </ul>
 *   </li>
 *   <li>⚠️ 注意：testCreateSchema 和 testUpdateSchema 会实际修改数据库，测试时需谨慎</li>
 *   <li>详细说明请参考：TEST_GUIDE.md</li>
 * </ol>
 * 
 * @author noear
 * @since 3.4
 */
@SolonTest(TestApp.class)
public class SchemaManagerTest {
    
    /**
     * 测试生成DDL脚本到文件
     */
    @Test
    public void testGenerateDdlToFile() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                System.out.println("未找到Hibernate适配器");
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            // 生成DDL到文件
            String outputFile = "target/schema_test.sql";
            schemaManager.generateDdlToFile(outputFile, true);
            
            System.out.println("✅ DDL脚本已生成到: " + outputFile);
            
            // 验证文件是否存在
            File file = new File(outputFile);
            if (file.exists()) {
                System.out.println("   文件大小: " + file.length() + " 字节");
            }
        } catch (Exception e) {
            System.err.println("❌ 生成DDL失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试生成DDL字符串
     */
    @Test
    public void testGenerateDdlString() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            // 生成DDL字符串
            String ddl = schemaManager.generateDdlString(true);
            
            System.out.println("✅ 生成的DDL长度: " + ddl.length() + " 字符");
            if (ddl.length() > 0) {
                System.out.println("   前100个字符: " + ddl.substring(0, Math.min(100, ddl.length())));
            }
        } catch (Exception e) {
            System.err.println("❌ 生成DDL失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试生成创建表的DDL
     */
    @Test
    public void testGenerateCreateDdl() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            String createDdl = schemaManager.generateCreateDdl();
            System.out.println("✅ 创建表的DDL已生成");
            System.out.println("   长度: " + createDdl.length() + " 字符");
        } catch (Exception e) {
            System.err.println("❌ 生成创建DDL失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试生成删除表的DDL
     */
    @Test
    public void testGenerateDropDdl() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            String dropDdl = schemaManager.generateDropDdl();
            System.out.println("✅ 删除表的DDL已生成");
            System.out.println("   长度: " + dropDdl.length() + " 字符");
        } catch (Exception e) {
            System.err.println("❌ 生成删除DDL失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试验证Schema
     */
    @Test
    public void testValidateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
            
            if (result.isValid()) {
                System.out.println("✅ Schema验证通过");
                System.out.println("   消息: " + result.getMessage());
            } else {
                System.out.println("❌ Schema验证失败");
                System.out.println("   消息: " + result.getMessage());
            }
        } catch (Exception e) {
            System.err.println("❌ 验证Schema失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试更新Schema（注意：会修改数据库）
     * 
     * ⚠️ 警告：此测试会实际修改数据库表结构，测试时需谨慎
     */
    @Test
    @Transaction
    public void testUpdateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            System.out.println("⚠️  开始更新Schema（会修改数据库）...");
            schemaManager.updateSchema();
            System.out.println("✅ Schema更新完成");
        } catch (Exception e) {
            System.err.println("❌ 更新Schema失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试创建Schema（注意：会创建表）
     * 
     * ⚠️ 警告：此测试会实际创建数据库表，测试时需谨慎
     */
    @Test
    @Transaction
    public void testCreateSchema() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            
            System.out.println("⚠️  开始创建Schema（会创建表）...");
            schemaManager.createSchema(false); // 不删除已存在的表
            System.out.println("✅ Schema创建完成");
        } catch (Exception e) {
            System.err.println("❌ 创建Schema失败: " + e.getMessage());
            System.err.println("   可能原因：表已存在，或数据库连接失败");
            e.printStackTrace();
        }
    }
}
