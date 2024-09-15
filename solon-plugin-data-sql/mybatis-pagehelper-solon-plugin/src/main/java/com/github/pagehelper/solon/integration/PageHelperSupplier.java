package com.github.pagehelper.solon.integration;

import com.github.pagehelper.PageInterceptor;

import java.util.function.Supplier;

/**
 * 分页插件提供者
 *
 * @author noear
 * @since 3.0
 */
public class PageHelperSupplier implements Supplier<PageInterceptor> {
    private final PageHelperProperties pageProperties;
    private PageInterceptor pageInterceptor;

    public PageHelperSupplier() {
        this(null);
    }

    public PageHelperSupplier(String propPrefix) {
        pageProperties = new PageHelperProperties(propPrefix);
    }

    /**
     * 配置
     */
    public PageHelperProperties config() {
        return pageProperties;
    }

    /**
     * 获取
     */
    @Override
    public PageInterceptor get() {
        if (pageInterceptor == null) {
            pageInterceptor = new PageInterceptor();
            pageInterceptor.setProperties(pageProperties.getProperties());
        }

        return pageInterceptor;
    }
}