package com.jfinal.plugin.activerecord.solon.integration;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import com.jfinal.plugin.activerecord.solon.ArpManager;
import com.jfinal.plugin.activerecord.solon.annotation.Db;
import com.jfinal.plugin.activerecord.solon.proxy.MapperInvocationHandler;
import org.noear.solon.data.datasource.DsInjector;

import java.lang.reflect.Proxy;

/**
 * @author noear
 * @since 1.10
 * @since 3.2
 */
public class DbBeanInjectorImpl extends DsInjector<Db> {
    public DbBeanInjectorImpl() {
        super(Db::value);

        addHandler(this::injectHandle);
    }

    /**
     * 字段注入
     *
     * @since 3.2
     */
    public boolean injectHandle(VarHolder vh, BeanWrap bw) {
        //如果是 DbPro
        if (DbPro.class.isAssignableFrom(vh.getType())) {
            String name = bw.name();
            if (bw.typed()) {
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
            return true;
        }

        //如果是 ActiveRecordPlugin
        if (ActiveRecordPlugin.class.isAssignableFrom(vh.getType())) {
            ActiveRecordPlugin arp = ArpManager.getOrAdd(bw.name(), bw);
            vh.setValue(arp);
            return true;
        }

        //如果是 interface，则为 Mapper 代理
        if (vh.getType().isInterface()) {
            MapperInvocationHandler handler = new MapperInvocationHandler(vh.getType(), bw.name());

            Object obj = Proxy.newProxyInstance(vh.context().getClassLoader(),
                    new Class[]{vh.getType()},
                    handler);

            vh.setValue(obj);
            return true;
        }

        return false;
    }
}