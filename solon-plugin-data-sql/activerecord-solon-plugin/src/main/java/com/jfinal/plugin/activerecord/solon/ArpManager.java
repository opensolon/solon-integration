package com.jfinal.plugin.activerecord.solon;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Model;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.ScanUtil;
import org.noear.solon.data.tran.DataSourceProxy;
import com.jfinal.plugin.activerecord.solon.annotation.Db;
import com.jfinal.plugin.activerecord.solon.annotation.Table;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ActiveRecordPlugin 管理器
 *
 * @author noear
 * @since 1.10
 */
public class ArpManager {
    private static Map<String, ActiveRecordPlugin> arpMap = new ConcurrentHashMap<>();
    private static Set<Runnable> startEvents = new LinkedHashSet<>();

    public static void addStartEvent(Runnable event) {
        startEvents.add(event);
    }

    /**
     * 添加数据源
     */
    public static void add(BeanWrap bw) {
        getOrAdd(bw.name(), bw);

        if (bw.typed()) {
            getOrAdd(DbKit.MAIN_CONFIG_NAME, bw);
        }
    }

    public static ActiveRecordPlugin getOrAdd(String name, BeanWrap bw) {
        if (Utils.isEmpty(name)) {
            name = DbKit.MAIN_CONFIG_NAME;
        }

        return arpMap.computeIfAbsent(name, k -> buildDo(k, bw.raw()));
    }

    /**
     * 获取 ActiveRecordPlugin
     */
    public static ActiveRecordPlugin get(String name) {
        if (Utils.isEmpty(name)) {
            name = DbKit.MAIN_CONFIG_NAME;
        }

        return arpMap.get(name);
    }

    private static ActiveRecordPlugin buildDo(String name, DataSource ds) {
        // 构建配置
        DataSource arpDs = new DataSourceProxy(ds);
        Config arpCfg = new ConfigImpl(name, arpDs, DbKit.DEFAULT_TRANSACTION_LEVEL);

        // 构建arp
        return new ActiveRecordPlugin(arpCfg);
    }


    /**
     * 开始构建 ActiveRecordPlugin 服务
     */
    public static void start() {
        List<String> sqlUrls = new ArrayList<>();

        // 添加SQL模板映射
        ScanUtil.scan("sql", n -> n.endsWith(".sql")).forEach(url -> {
            sqlUrls.add(url);
        });

        for (ActiveRecordPlugin arp : arpMap.values()) {
            // 添加表映射
            addTableMapping(arp);

            sqlUrls.forEach(url -> arp.addSqlTemplate(url));

            // 发布事件，以便扩展
            EventBus.publish(arp);

            // 启动 arp
            arp.start();
        }

        for (Runnable event : startEvents) {
            event.run();
        }
    }

    /**
     * 停止 ActiveRecordPlugin 实例
     */
    public static void stop() throws Throwable {
        for (ActiveRecordPlugin arp : arpMap.values()) {
            arp.stop();
        }
    }


    private static void addTableMapping(ActiveRecordPlugin arp) {
        // 取得config的名称，即数据源名称
        String dsName = arp.getConfig().getName();

        // 处理主数据源
        for (Map.Entry<Table, Class<? extends Model<?>>> entry : ModelManager.getModelClassMap().entrySet()) {
            // 根据数据源名称做不同处理
            Table table = entry.getKey();
            String dbSource = getDbSource(entry.getValue());
            if (dsName.equals(dbSource)) {
                //添加表映射
                arp.addMapping(table.name(), table.primaryKey(), entry.getValue());
            }
        }
    }


    private static String getDbSource(Class<? extends Model<?>> model) {
        Db db = model.getAnnotation(Db.class);
        if (null == db) {
            // 没有Db标签，默认为主数据库
            return DbKit.MAIN_CONFIG_NAME;
        }

        return db.value();
    }
}
