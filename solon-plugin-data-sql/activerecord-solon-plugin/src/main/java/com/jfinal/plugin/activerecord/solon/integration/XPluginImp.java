package com.jfinal.plugin.activerecord.solon.integration;

import javax.sql.DataSource;

import com.jfinal.plugin.activerecord.solon.ArpManager;
import com.jfinal.plugin.activerecord.solon.annotation.Db;
import com.jfinal.plugin.activerecord.solon.annotation.Table;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.LifecycleIndex;
import org.noear.solon.core.Plugin;
import org.noear.solon.data.datasource.DsInjector;

/**
 * @author noear
 * @since 1.4
 */
public class XPluginImp implements Plugin {

    @Override
    public void start(AppContext context) {
        // 构建Bean时的Table标签
        context.beanBuilderAdd(Table.class, new TableBeanBuilderImpl());

        // 注入Bean时的Db标签
        DbBeanInjectorImpl dbBeanInjector = new DbBeanInjectorImpl();
        context.beanInjectorAdd(Db.class, dbBeanInjector);
        DsInjector.getDefault().addHandler(dbBeanInjector::injectHandle);

        context.subWrapsOfType(DataSource.class, bw -> {
            ArpManager.add(bw);
        });

        // 通过DataSource类型获取Bean实例
        context.lifecycle(LifecycleIndex.PLUGIN_BEAN_USES, () -> {
            ArpManager.start();
        });
    }


    @Override
    public void prestop() throws Throwable {
        // 循环停止ActiveRecordPlugin
        ArpManager.stop();
    }
}