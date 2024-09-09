package org.noear.wood.solon.integration;

import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsUtils;
import org.noear.wood.BaseMapper;
import org.noear.wood.DbContext;
import org.noear.wood.annotation.Db;

/**
 * @author noear
 * @since 3.0
 * @deprecated 3.0
 */
@Deprecated
public class DbBeanInjectorImpl implements BeanInjector<Db> {
    @Override
    public void doInject(VarHolder vh, Db anno) {
        DsUtils.observeDs(vh.context(), anno.value(), dsWrap -> {
            inject0(vh, dsWrap);
        });
    }

    private void inject0(VarHolder varH, BeanWrap dsBw) {
        DbContext db = DbManager.global().get(dsBw);
        Class<?> clz = varH.getType();

        if (DbContext.class.isAssignableFrom(clz)) {
            varH.setValue(db);
        } else if (clz.isInterface()) {
            if (clz == BaseMapper.class) {
                Object obj = db.mapperBase((Class<?>) varH.getGenericType().getActualTypeArguments()[0]);
                varH.setValue(obj);
            } else {
                Object obj = db.mapper(clz);
                varH.setValue(obj);
            }
        }
    }
}
