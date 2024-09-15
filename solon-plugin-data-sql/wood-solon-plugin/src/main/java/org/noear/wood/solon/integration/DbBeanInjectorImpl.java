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
 */
public class DbBeanInjectorImpl implements BeanInjector<Db> {
    @Override
    public void doInject(VarHolder vh, Db anno) {
        vh.required(true);

        DsUtils.observeDs(vh.context(), anno.value(), dsWrap -> {
            inject0(vh, dsWrap);
        });
    }

    private void inject0(VarHolder vh, BeanWrap dsBw) {
        DbContext db = DbManager.global().get(dsBw);
        Class<?> clz = vh.getType();

        if (DbContext.class.isAssignableFrom(clz)) {
            vh.setValue(db);
        } else if (clz.isInterface()) {
            if (clz == BaseMapper.class) {
                Object obj = db.mapperBase((Class<?>) vh.getGenericType().getActualTypeArguments()[0]);
                vh.setValue(obj);
            } else {
                Object obj = db.mapper(clz);
                vh.setValue(obj);
            }
        }
    }
}
