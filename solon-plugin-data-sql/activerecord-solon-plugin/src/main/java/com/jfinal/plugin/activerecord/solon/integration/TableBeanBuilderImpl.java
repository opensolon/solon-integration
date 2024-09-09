package com.jfinal.plugin.activerecord.solon.integration;

import com.jfinal.plugin.activerecord.Model;
import org.noear.solon.core.BeanBuilder;
import org.noear.solon.core.BeanWrap;
import com.jfinal.plugin.activerecord.solon.ModelManager;
import com.jfinal.plugin.activerecord.solon.annotation.Table;

/**
 * @author noear
 * @since 1.10
 */
public class TableBeanBuilderImpl implements BeanBuilder<Table> {
    @Override
    public void doBuild(Class<?> clz, BeanWrap bw, Table anno) throws Throwable {
        if (!(bw.raw() instanceof Model)) {
            return;
        }

        ModelManager.addModel(anno, bw.raw());
    }
}
