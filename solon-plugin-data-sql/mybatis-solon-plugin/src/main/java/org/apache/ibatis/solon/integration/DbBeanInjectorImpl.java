package org.apache.ibatis.solon.integration;

import org.apache.ibatis.solon.MybatisAdapter;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsInjector;

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
        MybatisAdapter adapter = MybatisAdapterManager.get(dsBw);

        if (adapter != null) {
            adapter.injectTo(vh);
        }
    }
}
