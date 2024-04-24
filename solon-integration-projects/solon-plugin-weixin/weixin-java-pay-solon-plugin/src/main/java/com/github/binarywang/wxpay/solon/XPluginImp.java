package com.github.binarywang.wxpay.solon;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.LifecycleIndex;
import org.noear.solon.core.Plugin;
import com.github.binarywang.wxpay.solon.config.WxPayAutoConfiguration;
import com.github.binarywang.wxpay.solon.properties.WxPayProperties;

public class XPluginImp implements Plugin{

	@Override
	public void start(AppContext context) throws Throwable {
		context.lifecycle(LifecycleIndex.PLUGIN_BEAN_USES, () -> {
            context.beanMake(WxPayProperties.class);
            context.beanMake(WxPayAutoConfiguration.class);
        });
	}

}
