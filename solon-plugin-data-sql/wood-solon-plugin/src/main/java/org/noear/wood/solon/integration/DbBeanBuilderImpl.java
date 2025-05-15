package org.noear.wood.solon.integration;

import org.noear.solon.core.BeanWrap;
import org.noear.solon.data.datasource.DsBuilder;
import org.noear.wood.DbContext;
import org.noear.wood.annotation.Db;

/**
 * @author noear
 * @since 3.0
 * @deprecated 3.2 不再支持加在 Mapper 上（只能通过配置）
 */
@Deprecated
public class DbBeanBuilderImpl extends DsBuilder<Db> {
    public DbBeanBuilderImpl() {
        super(Db::value);

        addHandler(this::buildHandle);
    }

    public boolean buildHandle(Class<?> clz, BeanWrap dsWrap) {
        if (clz.isInterface()) {
            DbContext db = DbManager.global().get(dsWrap);
            dsWrap.context().wrapAndPut(clz, db.mapper(clz));
            return true;
        }

        return false;
    }
}