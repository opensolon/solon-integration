package org.hibernate.solon.test;

import org.hibernate.solon.integration.HibernateAdapter;
import org.hibernate.solon.integration.HibernateAdapterManager;
import org.hibernate.solon.integration.schema.DdlGenerator;
import org.junit.jupiter.api.Test;
import org.noear.solon.test.SolonTest;

import java.io.File;

/**
 * DDL生成器测试类
 * 
 * <p><b>⚠️ 测试说明：</b></p>
 * <ol>
 *   <li>此测试类不需要数据库表已存在，它会生成DDL脚本</li>
 *   <li>生成的DDL脚本可用于创建数据库表结构</li>
 *   <li>生成的文件位置：target/schema.sql</li>
 *   <li>生成DDL后，可以手动执行SQL脚本创建表</li>
 *   <li>详细说明请参考：TEST_GUIDE.md</li>
 * </ol>
 * 
 * @author noear
 * @since 3.4
 */
@SolonTest(TestApp.class)
public class DdlGeneratorTest {
    
    /**
     * 测试生成DDL脚本到文件
     */
    @Test
    public void testGenerateDdlToFile() {
        try {
            // 获取默认的HibernateAdapter
            HibernateAdapter adapter = HibernateAdapterManager.getOnly("");
            if (adapter == null) {
                System.out.println("未找到Hibernate适配器");
                return;
            }
            
            DdlGenerator generator = adapter.getDdlGenerator();
            
            // 生成DDL到文件
            String outputFile = "target/schema.sql";
            generator.generateDdlToFile(outputFile, true);
            
            System.out.println("DDL脚本已生成到: " + outputFile);
            
            // 验证文件是否存在
            File file = new File(outputFile);
            if (file.exists()) {
                System.out.println("文件大小: " + file.length() + " 字节");
            }
        } catch (Exception e) {
            System.err.println("生成DDL失败: " + e.getMessage());
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
                System.out.println("未找到Hibernate适配器");
                return;
            }
            
            DdlGenerator generator = adapter.getDdlGenerator();
            
            // 生成DDL字符串
            String ddl = generator.generateDdlString(true);
            
            System.out.println("生成的DDL:");
            System.out.println(ddl);
        } catch (Exception e) {
            System.err.println("生成DDL失败: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println("未找到Hibernate适配器");
                return;
            }
            
            DdlGenerator generator = adapter.getDdlGenerator();
            
            String createDdl = generator.generateCreateDdl();
            System.out.println("创建表的DDL:");
            System.out.println(createDdl);
        } catch (Exception e) {
            System.err.println("生成创建DDL失败: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println("未找到Hibernate适配器");
                return;
            }
            
            DdlGenerator generator = adapter.getDdlGenerator();
            
            String dropDdl = generator.generateDropDdl();
            System.out.println("删除表的DDL:");
            System.out.println(dropDdl);
        } catch (Exception e) {
            System.err.println("生成删除DDL失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

