package org.apache.ibatis.solon.integration;

import org.apache.ibatis.plugin.Interceptor;
import org.noear.solon.Solon;

import java.util.List;

/**
 * 插件管理器
 *
 * @author noear
 * @since 1.5
 */
public class MybatisPluginManager {
    private static List<Interceptor> interceptors;

    /**
     * 获取全局插件
     */
    public static List<Interceptor> getInterceptors() {
        tryInit();

        return interceptors;
    }

    /**
     * 添加全局插件
     */
    public static void addInterceptor(Interceptor interceptor) {
        getInterceptors().add(interceptor);
    }

    /**
     * 初始化
     */
    private static void tryInit() {
        if (interceptors != null) {
            return;
        }

        //支持两种配置
        interceptors = MybatisPluginUtils.resolve(Solon.cfg(), "mybatis.plugin");
        if (interceptors.size() == 0) {
            //新加
            interceptors = MybatisPluginUtils.resolve(Solon.cfg(), "mybatis.plugins");
        }
    }
}