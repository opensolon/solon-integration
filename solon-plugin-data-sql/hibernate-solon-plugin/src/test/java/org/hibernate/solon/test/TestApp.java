package org.hibernate.solon.test;

import org.hibernate.solon.annotation.EnableHibernate;
import org.noear.solon.Solon;

/**
 * 测试应用启动类
 * 
 * <p>使用@EnableHibernate注解启用Hibernate功能</p>
 * 
 * @author noear
 * @since 3.4
 */
@EnableHibernate(
    basePackages = "org.hibernate.solon.test.entity",
    autoScanEntities = true,
    showSql = true
)
public class TestApp {
    public static void main(String[] args) {
        Solon.start(TestApp.class, args);
    }
}

