package org.hibernate.solon.integration;

import org.hibernate.solon.annotation.Db;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsUtils;

/**
 * @author lingkang
 * @since 2.5
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
        HibernateAdapter adapter = HibernateAdapterManager.get(dsBw);

        if (adapter != null) {
            adapter.injectTo(vh);
        }
    }
}
