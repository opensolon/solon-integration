package com.baomidou.mybatisplus.solon.integration;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.solon.integration.aot.MybatisPlusRuntimeNativeRegistrar;
import org.apache.ibatis.solon.integration.DsBeanInjectorImpl;
import org.apache.ibatis.solon.integration.MybatisAdapterManager;
import org.noear.solon.aot.RuntimeNativeRegistrar;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.runtime.NativeDetector;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.core.util.GenericUtil;
import org.noear.solon.core.Plugin;
import org.noear.solon.data.annotation.Ds;

/**
 * @author noear
 * @since 1.5
 */
public class XPluginImpl implements Plugin {
    @Override
    public void start(AppContext context) {
        //
        // 此插件的 solon.plugin.priority 会大于 mybatis-solon-plugin 的值
        //
        MybatisAdapterManager.setAdapterFactory(new MybatisAdapterFactoryPlus());
        GenericTypeUtils.setGenericTypeResolver(((clazz, genericIfc) ->
                GenericUtil.resolveTypeArguments(clazz, genericIfc)));

        // aot
        if (NativeDetector.isAotRuntime() && ClassUtil.hasClass(() -> RuntimeNativeRegistrar.class)) {
            context.wrapAndPut(MybatisPlusRuntimeNativeRegistrar.class);
        }

        //@since 2.9
        DsBeanInjectorImpl dsInjector = new DsBeanInjectorImpl();
        context.beanInjectorAdd(Ds.class, GlobalConfig.class, dsInjector);
        context.beanInjectorAdd(Ds.class, Mapper.class, dsInjector);
    }
}
