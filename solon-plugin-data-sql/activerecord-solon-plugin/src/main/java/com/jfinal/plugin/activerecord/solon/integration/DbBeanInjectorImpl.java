package com.jfinal.plugin.activerecord.solon.integration;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import com.jfinal.plugin.activerecord.solon.ArpManager;
import com.jfinal.plugin.activerecord.solon.annotation.Db;
import com.jfinal.plugin.activerecord.solon.proxy.MapperInvocationHandler;
import org.noear.solon.data.datasource.DsUtils;

import java.lang.reflect.Proxy;

/**
 * @author noear
 * @since 1.10
 */
public class DbBeanInjectorImpl implements BeanInjector<Db> {
    @Override
    public void doInject(VarHolder vh, Db anno) {
        vh.required(true);

        DsUtils.observeDs(vh.context(), anno.value(), (dsWrap) -> {
            this.injectDo(vh, anno.value(), dsWrap);
        });
    }

    /**
     * 字段注入
     */
    private void injectDo(VarHolder vh, String name, BeanWrap bw) {
        //如果是 DbPro
        if (DbPro.class.isAssignableFrom(vh.getType())) {
            if (Utils.isEmpty(name)) {
                name = DbKit.MAIN_CONFIG_NAME;
            }

            Config config = DbKit.getConfig(name);
            if (config != null) {
                vh.setValue(com.jfinal.plugin.activerecord.Db.use(name));
            } else {
                String name2 = name;
                ArpManager.addStartEvent(() -> {
                    vh.setValue(com.jfinal.plugin.activerecord.Db.use(name2));
                });
            }
            return;
        }

        //如果是 ActiveRecordPlugin
        if (ActiveRecordPlugin.class.isAssignableFrom(vh.getType())) {
            ActiveRecordPlugin arp = ArpManager.getOrAdd(name, bw);
            vh.setValue(arp);
            return;
        }

        //如果是 interface，则为 Mapper 代理
        if (vh.getType().isInterface()) {
            MapperInvocationHandler handler = new MapperInvocationHandler(vh.getType(), name);

            Object obj = Proxy.newProxyInstance(vh.context().getClassLoader(),
                    new Class[]{vh.getType()},
                    handler);

            vh.setValue(obj);
            return;
        }
    }
}
