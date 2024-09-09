package org.apache.ibatis.solon.integration;

import org.apache.ibatis.solon.MybatisAdapter;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.annotation.Ds;
import org.noear.solon.data.datasource.DsUtils;

/**
 * @author noear
 * @since 2.9
 */
public class DsBeanInjectorImpl implements BeanInjector<Ds> {
    @Override
    public void doInject(VarHolder vh, Ds anno) {
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
