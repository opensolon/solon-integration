package org.noear.wood.solon.integration;

import org.noear.solon.core.BeanBuilder;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.data.datasource.DsUtils;
import org.noear.wood.DbContext;
import org.noear.wood.annotation.Db;

/**
 * @author noear
 * @since 2.9
 * @deprecated 2.9
 */
@Deprecated
public class DbBeanBuilderImpl implements BeanBuilder<Db> {
    @Override
    public void doBuild(Class<?> clz, BeanWrap bw, Db anno) throws Throwable {
        if (clz.isInterface() == false) {
            return;
        }

        DsUtils.observeDs(bw.context(), anno.value(), dsWrap -> {
            create0(clz, dsWrap);
        });
    }

    private void create0(Class<?> clz, BeanWrap dsBw) {
        DbContext db = DbManager.global().get(dsBw);
        dsBw.context().wrapAndPut(clz, db.mapper(clz));
    }
}