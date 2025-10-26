package org.noear.wood.solon.integration;

import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsInjector;
import org.noear.wood.BaseMapper;
import org.noear.wood.DbContext;
import org.noear.wood.annotation.Db;

/**
 * @author noear
 * @since 3.0
 */
public class DbBeanInjectorImpl extends DsInjector<Db> {
    public DbBeanInjectorImpl() {
        super(Db::value);

        addHandler(this::injectHandle);
    }

    public void injectHandle(VarHolder vh, BeanWrap dsBw) {
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
