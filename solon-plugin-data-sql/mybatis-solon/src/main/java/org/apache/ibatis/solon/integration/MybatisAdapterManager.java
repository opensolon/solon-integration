package org.apache.ibatis.solon.integration;

import org.noear.solon.Utils;
import org.noear.solon.core.BeanWrap;
import org.apache.ibatis.solon.MybatisAdapter;
import org.apache.ibatis.solon.MybatisAdapterFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 适配管理器
 *
 * @author noear
 * @since 1.1
 * */
public class MybatisAdapterManager {
    private static MybatisAdapterFactory adapterFactory = new MybatisAdapterFactoryDefault();

    /**
     * 设置适配器工厂
     */
    public static void setAdapterFactory(MybatisAdapterFactory adapterFactory) {
        MybatisAdapterManager.adapterFactory = adapterFactory;
    }

    /**
     * 缓存适配器
     */
    private static final Map<String, MybatisAdapter> dbMap = new ConcurrentHashMap<>();

    public static MybatisAdapter getOnly(String name) {
        return dbMap.get(name);
    }

    public static Map<String, MybatisAdapter> getAll() {
        return Collections.unmodifiableMap(dbMap);
    }

    /**
     * 获取适配器
     */
    public static MybatisAdapter get(BeanWrap bw) {
        MybatisAdapter db = dbMap.get(bw.name());

        if (db == null) {
            Utils.locker().lock();

            try {
                db = dbMap.get(bw.name());
                if (db == null) {
                    db = buildAdapter(bw);

                    register(bw, db);
                }
            } finally {
                Utils.locker().unlock();
            }
        }

        return db;
    }

    /**
     * 注册适配器
     */
    public static void register(BeanWrap bw, MybatisAdapter adapter) {
        dbMap.put(bw.name(), adapter);

        if (bw.typed()) {
            dbMap.put("", adapter);
        }
    }

    /**
     * 注册数据源，并生成适配器
     *
     * @param bw 数据源的BW
     */
    public static void register(BeanWrap bw) {
        get(bw);
    }

    /**
     * 构建适配器
     */
    private static MybatisAdapter buildAdapter(BeanWrap bw) {
        MybatisAdapter tmp;

        if (Utils.isEmpty(bw.name())) {
            tmp = adapterFactory.create(bw);
        } else {
            tmp = adapterFactory.create(bw, bw.context().cfg().getProp("mybatis." + bw.name()));
        }

        if (tmp instanceof MybatisAdapterDefault) {
            ((MybatisAdapterDefault) tmp).mapperPublish();
        }

        return tmp;
    }
}