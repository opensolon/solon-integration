package org.apache.ibatis.solon.integration;

import org.apache.ibatis.solon.annotation.Db;
import org.apache.ibatis.solon.aot.MybatisRuntimeNativeRegistrar;
import org.noear.solon.aot.RuntimeNativeRegistrar;
import org.noear.solon.core.*;
import org.noear.solon.core.runtime.NativeDetector;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.data.datasource.DsInjector;

import javax.sql.DataSource;

public class XPluginImpl implements Plugin {
    @Override
    public void start(AppContext context) {

        context.subWrapsOfType(DataSource.class, bw -> {
            MybatisAdapterManager.register(bw);
        });

        //for db
        context.beanBuilderAdd(Db.class, new DbBeanBuilderImpl());

        DbBeanInjectorImpl dbBeanInjector = new DbBeanInjectorImpl();
        context.beanInjectorAdd(Db.class, dbBeanInjector);
        DsInjector.getDefault().addHandler(dbBeanInjector::injectHandle);

        // aot
        if (NativeDetector.isAotRuntime() && ClassUtil.hasClass(() -> RuntimeNativeRegistrar.class)) {
            context.wrapAndPut(MybatisRuntimeNativeRegistrar.class);
        }

    }
}
