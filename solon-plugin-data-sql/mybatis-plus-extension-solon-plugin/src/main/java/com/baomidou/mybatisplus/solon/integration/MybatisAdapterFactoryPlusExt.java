package com.baomidou.mybatisplus.solon.integration;

import org.apache.ibatis.solon.MybatisAdapter;
import org.apache.ibatis.solon.MybatisAdapterFactory;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;

/**
 * 适配器工厂 for mybatis-plus
 *
 * @author noear, iYarnFog
 * @since 1.5
 */
public class MybatisAdapterFactoryPlusExt implements MybatisAdapterFactory {
    @Override
    public MybatisAdapter create(BeanWrap dsWrap) {
        return new MybatisAdapterPlusExt(dsWrap);
    }

    @Override
    public MybatisAdapter create(BeanWrap dsWrap, Props dsProps) {
        return new MybatisAdapterPlusExt(dsWrap, dsProps);
    }
}
