package org.apache.ibatis.solon.integration;

import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.core.BeanBuilder;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.data.datasource.DsUtils;

/**
 * @author noear
 * @since 3.0
 * @deprecated 3.0
 */
@Deprecated
public class DbBeanBuilderImpl implements BeanBuilder<Db> {
    @Override
    public void doBuild(Class<?> clz, BeanWrap bw, Db anno) throws Throwable {
        if (clz.isInterface() == false) {
            return;
        }

        DsUtils.observeDs(bw.context(), anno.value(), (dsWrap) -> {
            create0(clz, dsWrap);
        });
    }

    private void create0(Class<?> clz, BeanWrap dsBw) {
        Object raw = MybatisAdapterManager.get(dsBw).getMapper(clz);
        dsBw.context().wrapAndPut(clz, raw);
    }
}
