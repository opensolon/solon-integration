package com.baomidou.mybatisplus.solon.integration;

import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.solon.integration.aot.MybatisPlusRuntimeNativeRegistrar;
import org.apache.ibatis.solon.integration.MybatisAdapterManager;
import org.noear.solon.aot.RuntimeNativeRegistrar;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.runtime.NativeDetector;
import org.noear.solon.core.util.ClassUtil;
import org.noear.solon.core.util.GenericUtil;
import org.noear.solon.core.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author noear
 * @since 1.5
 */
public class MybatisplusPlugin implements Plugin {
    private Logger log;

    @Override
    public void start(AppContext context) {
        if (log == null) {
            log = LoggerFactory.getLogger(MybatisplusPlugin.class);
            log.warn("The artifact 'org.noear:mybatis-plus-solon-plugin' is deprecated, suggest use 'com.baomidou:mybatis-plus-solon-plugin'.");
        }

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
    }
}
