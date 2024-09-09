package org.apache.ibatis.solon.integration;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.solon.annotation.Db;
import org.apache.ibatis.solon.annotation.Mappable;
import org.apache.ibatis.solon.aot.MybatisRuntimeNativeRegistrar;
import org.noear.solon.aot.RuntimeNativeRegistrar;
import org.noear.solon.core.*;
import org.apache.ibatis.solon.MybatisAdapter;
import org.noear.solon.core.runtime.NativeDetector;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.data.annotation.Ds;

import javax.sql.DataSource;

public class XPluginImpl implements Plugin {
    @Override
    public void start(AppContext context) {

        context.subWrapsOfType(DataSource.class, bw -> {
            MybatisAdapterManager.register(bw);
        });

        //@since 2.9
        DsBeanInjectorImpl dsInjector = new DsBeanInjectorImpl();
        context.beanInjectorAdd(Ds.class, MybatisAdapter.class, dsInjector);
        context.beanInjectorAdd(Ds.class, SqlSessionFactory.class, dsInjector);
        context.beanInjectorAdd(Ds.class, Configuration.class, dsInjector);
        context.beanInjectorAdd(Ds.class, Mappable.class, dsInjector);

        ///////////////////////////////////////

        //for db
        context.beanBuilderAdd(Db.class, new DbBeanBuilderImpl());
        context.beanInjectorAdd(Db.class, new DbBeanInjectorImpl());

        // aot
        if (NativeDetector.isAotRuntime() && ClassUtil.hasClass(() -> RuntimeNativeRegistrar.class)) {
            context.wrapAndPut(MybatisRuntimeNativeRegistrar.class);
        }

    }
}
