package com.drools.solon;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.LifecycleIndex;
import org.noear.solon.core.Plugin;

import com.drools.solon.config.DroolsAutoConfiguration;

public class XpluginImp implements Plugin {
    @Override
    public void start(AppContext context) throws Throwable {
    	context.lifecycle(LifecycleIndex.PLUGIN_BEAN_USES, () -> {
    		context.beanMake(DroolsAutoConfiguration.class);
    	});
    }
}
