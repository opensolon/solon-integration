package com.jfinal.plugin.activerecord.solon.integration;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.annotation.Ds;
import org.noear.solon.data.datasource.DsUtils;
import com.jfinal.plugin.activerecord.solon.ArpManager;
import com.jfinal.plugin.activerecord.solon.proxy.MapperInvocationHandler;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;

/**
 * @author noear
 * @since 2.9
 */
public class DsBeanInjectorImpl implements BeanInjector<Ds> {
    @Override
    public void doInject(VarHolder vh, Ds anno) {
        DsUtils.observeDs(vh.context(), anno.value(), (dsWrap) -> {
            inject0(vh, anno.value(), dsWrap.get());
        });
    }

    /**
     * 字段注入
     */
    private void inject0(VarHolder varH, String name, DataSource ds) {
        //如果是 DbPro
        if (DbPro.class.isAssignableFrom(varH.getType())) {
            if (Utils.isEmpty(name)) {
                name = DbKit.MAIN_CONFIG_NAME;
            }

            Config config = DbKit.getConfig(name);
            if (config != null) {
                varH.setValue(com.jfinal.plugin.activerecord.Db.use(name));
            } else {
                String name2 = name;
                ArpManager.addStartEvent(() -> {
                    varH.setValue(com.jfinal.plugin.activerecord.Db.use(name2));
                });
            }
            return;
        }

        //如果是 ActiveRecordPlugin
        if (ActiveRecordPlugin.class.isAssignableFrom(varH.getType())) {
            ActiveRecordPlugin arp = ArpManager.getOrAdd(name, ds);
            varH.setValue(arp);
            return;
        }

        //如果是 interface，则为 Mapper 代理
        if (varH.getType().isInterface()) {
            MapperInvocationHandler handler = new MapperInvocationHandler(varH.getType(), name);

            Object obj = Proxy.newProxyInstance(varH.context().getClassLoader(),
                    new Class[]{varH.getType()},
                    handler);

            varH.setValue(obj);
            return;
        }
    }
}