package org.apache.ibatis.solon.integration;

import org.apache.ibatis.solon.MybatisAdapter;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsUtils;

/**
 * @author noear
 * @since 3.0
 */
public class DbBeanInjectorImpl implements BeanInjector<Db> {
    @Override
    public void doInject(VarHolder vh, Db anno) {
        vh.required(true);

        DsUtils.observeDs(vh.context(), anno.value(), (dsWrap) -> {
            inject0(vh, dsWrap);
        });
    }

    private void inject0(VarHolder varH, BeanWrap dsBw) {
        MybatisAdapter adapter = MybatisAdapterManager.get(dsBw);

        if (adapter != null) {
            adapter.injectTo(varH);
        }
    }
}
