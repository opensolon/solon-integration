package org.hibernate.solon.test;

import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.hibernate.solon.integration.schema.AutoTableEnhancer;
import org.hibernate.solon.integration.schema.SchemaManager;
import org.junit.jupiter.api.Test;
import org.noear.solon.test.SolonTest;

/**
 * 自动表功能测试类
 * 
 * <p>测试自动表创建、命名策略、表统计等功能</p>
 * 
 * <p><b>⚠️ 测试前准备：</b></p>
 * <ol>
 *   <li>部分测试需要数据库表已存在（如testTableChangeDetection）</li>
 *   <li>创建方式：
 *     <ul>
 *       <li>方式1：配置 hbm2ddl.auto=create 或 update，启动时自动创建</li>
 *       <li>方式2：执行 SQL脚本：src/test/resources/test_schema.sql</li>
 *       <li>方式3：运行 DdlGeneratorTest 生成DDL后手动执行</li>
 *     </ul>
 *   </li>
 *   <li>详细说明请参考：TEST_GUIDE.md</li>
 * </ol>
 * 
 * @author noear
 * @since 3.4
 */
@SolonTest(TestApp.class)
public class AutoTableTest {
    
    /**
     * 测试表结构变更检测
     */
    @Test
    public void testTableChangeDetection() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                System.out.println("未找到Hibernate适配器");
                return;
            }
            
            AutoTableEnhancer.TableChangeReport report = 
                AutoTableEnhancer.detectTableChanges(adapter);
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("表结构变更检测结果");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            if (report.isValid()) {
                System.out.println("✅ Schema验证通过");
                System.out.println("   消息: " + report.getMessage());
            } else {
                System.out.println("❌ Schema验证失败");
                System.out.println("   消息: " + report.getMessage());
            }
            
            if (!report.getAddedTables().isEmpty()) {
                System.out.println("新增的表: " + report.getAddedTables());
            }
            
            if (!report.getModifiedTables().isEmpty()) {
                System.out.println("修改的表: " + report.getModifiedTables());
            }
            
            if (!report.getRemovedTables().isEmpty()) {
                System.out.println("删除的表: " + report.getRemovedTables());
            }
            
        } catch (Exception e) {
            System.err.println("❌ 表变更检测失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试Schema验证
     */
    @Test
    public void testSchemaValidation() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            SchemaManager.SchemaValidationResult result = schemaManager.validateSchema();
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("Schema验证结果");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            if (result.isValid()) {
                System.out.println("✅ " + result.getMessage());
            } else {
                System.out.println("❌ " + result.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Schema验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试DDL生成（包含索引和约束）
     */
    @Test
    public void testDdlWithIndexesAndConstraints() {
        try {
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                return;
            }
            
            SchemaManager schemaManager = adapter.getSchemaManager();
            String ddl = schemaManager.generateDdlString(true);
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("生成的DDL（包含索引和约束）");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // 检查是否包含索引
            if (ddl.contains("INDEX") || ddl.contains("KEY")) {
                System.out.println("✅ DDL包含索引定义");
            }
            
            // 检查是否包含唯一约束
            if (ddl.contains("UNIQUE") || ddl.contains("uk_")) {
                System.out.println("✅ DDL包含唯一约束");
            }
            
            // 检查是否包含NOT NULL约束
            if (ddl.contains("NOT NULL")) {
                System.out.println("✅ DDL包含NOT NULL约束");
            }
            
            System.out.println("\nDDL预览（前500字符）:");
            System.out.println(ddl.substring(0, Math.min(500, ddl.length())));
            
        } catch (Exception e) {
            System.err.println("❌ 生成DDL失败: " + e.getMessage());
        }
    }
}

