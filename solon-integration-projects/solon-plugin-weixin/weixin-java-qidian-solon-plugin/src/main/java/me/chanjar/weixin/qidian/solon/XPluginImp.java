package me.chanjar.weixin.qidian.solon;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.LifecycleIndex;
import org.noear.solon.core.Plugin;
import me.chanjar.weixin.qidian.solon.config.WxQidianServiceAutoConfiguration;
import me.chanjar.weixin.qidian.solon.config.storage.WxQidianInJedisConfigStorageConfiguration;
import me.chanjar.weixin.qidian.solon.config.storage.WxQidianInMemoryConfigStorageConfiguration;
import me.chanjar.weixin.qidian.solon.config.storage.WxQidianInRedissonConfigStorageConfiguration;
import me.chanjar.weixin.qidian.solon.properties.WxQidianProperties;

public class XPluginImp implements Plugin{

	@Override
	public void start(AppContext context) throws Throwable {
		context.lifecycle(LifecycleIndex.PLUGIN_BEAN_USES, () -> {
            context.beanMake(WxQidianProperties.class);
            context.beanMake(WxQidianInMemoryConfigStorageConfiguration.class);
            context.beanMake(WxQidianInRedissonConfigStorageConfiguration.class);
            context.beanMake(WxQidianInJedisConfigStorageConfiguration.class);
            context.beanMake(WxQidianServiceAutoConfiguration.class);
        });
	}

}
