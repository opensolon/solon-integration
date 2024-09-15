package com.github.pagehelper.solon.integration;

import org.apache.ibatis.session.Configuration;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.event.EventListener;

/**
 * PageHelper 分布插件配置器（添加拦截器）
 *
 * @author noear
 * @since 1.5
 */
@Component
public class PageHelperConfiguration implements EventListener<Configuration> {
    private PageHelperSupplier pageHelperSupplier = new PageHelperSupplier();

    @Override
    public void onEvent(Configuration configuration) {
        if (!configuration.getInterceptors().contains(pageHelperSupplier.get())) {
            configuration.addInterceptor(pageHelperSupplier.get());
        }
    }
}
