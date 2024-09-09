package org.noear.wood.solon.integration;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.data.annotation.Ds;
import org.noear.wood.BaseMapper;
import org.noear.wood.DbContext;
import org.noear.wood.WoodConfig;
import org.noear.wood.annotation.Db;
import org.noear.wood.solon.annotation.Mappable;
import org.noear.wood.xml.XmlSqlLoader;

import javax.sql.DataSource;

/**
 * @author noear
 * @since 1.10
 */
public class XPluginImp implements Plugin {
    @Override
    public void start(AppContext context) {
        // 事件监听，用于时实初始化
        context.subWrapsOfType(DataSource.class, bw -> {
            DbManager.global().reg(bw);
        });

        // 切换Weed的链接工厂，交于Solon托管这
        WoodConfig.connectionFactory = new DsConnectionFactoryImpl();

        // 添加响应器（构建、注入）
        context.beanBuilderAdd(Db.class, new DbBeanBuilderImpl());
        context.beanInjectorAdd(Db.class, new DbBeanInjectorImpl());

        //@since 2.9
        DsBeanInjectorImpl injector = new DsBeanInjectorImpl();
        context.beanInjectorAdd(Ds.class, DbContext.class, injector);
        context.beanInjectorAdd(Ds.class, BaseMapper.class, injector);
        context.beanInjectorAdd(Ds.class, Mappable.class, injector);

        // 加载xml sql
        if (ClassUtil.hasClass(() -> XmlSqlLoader.class)) {
            XmlSqlLoader.tryLoad();
        }
    }
}
