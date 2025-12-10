package org.hibernate.solon.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.solon.annotation.Db;
import org.hibernate.solon.integration.query.DynamicQueryBuilder;
import org.hibernate.solon.integration.query.HibernateQueryHelper;
import org.hibernate.solon.integration.query.PageQuery;
import org.hibernate.solon.test.entity.User;
import org.junit.jupiter.api.Test;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.test.SolonTest;

import java.util.List;
import java.util.Map;

/**
 * 查询助手测试类
 * 
 * <p><b>⚠️ 测试前准备：</b></p>
 * <ol>
 *   <li>确保数据库表已创建（test_user表）</li>
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
public class QueryHelperTest {
    
    @Db
    @Inject
    private SessionFactory sessionFactory;
    
    /**
     * 测试基本查询
     */
    @Test
    @Transaction
    public void testBasicQuery() {
        Session session = sessionFactory.getCurrentSession();
        HibernateQueryHelper helper = new HibernateQueryHelper(session);
        
        // 查询所有用户
        List<User> users = helper.list("FROM User", User.class);
        System.out.println("查询到 " + users.size() + " 个用户");
        
        // 带参数的查询
        List<User> usersByName = helper.list(
            "FROM User WHERE name = :name",
            User.class,
            Map.of("name", "测试用户1")
        );
        System.out.println("查询到 " + usersByName.size() + " 个匹配用户");
    }
    
    /**
     * 测试分页查询
     */
    @Test
    @Transaction
    public void testPageQuery() {
        Session session = sessionFactory.getCurrentSession();
        HibernateQueryHelper helper = new HibernateQueryHelper(session);
        
        // 分页查询
        PageQuery<User> page = helper.pageQuery(
            "FROM User",
            User.class,
            1,  // 第1页
            10  // 每页10条
        );
        
        System.out.println("总记录数: " + page.getTotal());
        System.out.println("当前页: " + page.getPage());
        System.out.println("每页大小: " + page.getSize());
        System.out.println("总页数: " + page.getTotalPages());
        System.out.println("数据: " + page.getContent().size() + " 条");
    }
    
    /**
     * 测试动态查询构建器
     */
    @Test
    @Transaction
    public void testDynamicQuery() {
        Session session = sessionFactory.getCurrentSession();
        HibernateQueryHelper helper = new HibernateQueryHelper(session);
        
        // 构建动态查询
        DynamicQueryBuilder builder = new DynamicQueryBuilder("FROM User u");
        builder.like("u.name", "name", "测试")
               .where("u.age >= :age", "age", 20)
               .orderBy("u.createTime DESC");
        
        String hql = builder.build();
        Map<String, Object> params = builder.getParameters();
        
        System.out.println("生成的HQL: " + hql);
        System.out.println("参数: " + params);
        
        // 执行查询
        List<User> users = helper.list(hql, User.class, params);
        System.out.println("查询到 " + users.size() + " 个用户");
    }
}

