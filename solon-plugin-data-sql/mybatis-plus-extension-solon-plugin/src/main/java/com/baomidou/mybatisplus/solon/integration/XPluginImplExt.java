package com.baomidou.mybatisplus.solon.integration;

import org.apache.ibatis.solon.integration.MybatisAdapterManager;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * @author noear
 * @since 2.8
 */
public class XPluginImplExt implements Plugin {
    @Override
    public void start(AppContext context) {
        //
        // 此插件的 solon.plugin.priority 会大于 mybatis-solon-plugin 的值
        //
        MybatisAdapterManager.setAdapterFactory(new MybatisAdapterFactoryPlusExt());
    }
}
