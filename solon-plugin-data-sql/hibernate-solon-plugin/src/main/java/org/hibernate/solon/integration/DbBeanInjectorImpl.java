package org.hibernate.solon.integration;

import org.hibernate.solon.annotation.Db;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsInjector;

/**
 * @author lingkang
 * @since 2.5
 */
public class DbBeanInjectorImpl extends DsInjector<Db> {
    public DbBeanInjectorImpl() {
        super(Db::value);

        addHandler(this::injectHandle);
    }

    public boolean injectHandle(VarHolder vh, BeanWrap dsBw) {
        HibernateAdapter adapter = HibernateAdapterManager.get(dsBw);

        if (adapter != null) {
            adapter.injectTo(vh);
        }

        return vh.isDone();
    }
}