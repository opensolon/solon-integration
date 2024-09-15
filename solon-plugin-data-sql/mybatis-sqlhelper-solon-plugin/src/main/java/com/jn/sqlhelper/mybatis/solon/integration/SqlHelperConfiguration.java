package com.jn.sqlhelper.mybatis.solon.integration;

import com.jn.sqlhelper.mybatis.MybatisUtils;
import com.jn.sqlhelper.mybatis.plugins.CustomScriptLanguageDriver;
import com.jn.sqlhelper.mybatis.plugins.SqlHelperMybatisPlugin;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.noear.solon.annotation.Bean;
import org.noear.solon.core.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SqlHelper 分布插件配置器（添加拦截器）
 *
 * @author noear
 * @since 1.1
 * */
@org.noear.solon.annotation.Configuration
public class SqlHelperConfiguration implements EventListener<Configuration> {
    private static final Logger log = LoggerFactory.getLogger(SqlHelperConfiguration.class);
    private SqlHelperSupplier sqlHelperSupplier = new SqlHelperSupplier();

    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        return MybatisUtils.vendorDatabaseIdProvider();
    }

    @Bean
    public LanguageDriver customScriptLanguageDriver() {
        return new CustomScriptLanguageDriver();
    }

    @Override
    public void onEvent(Configuration configuration) {
        if (sqlHelperSupplier.get() == null) {
            return;
        }

        log.info("Mybatis: Start to customize mybatis configuration with mybatis-sqlhelper-solon-plugin");
        configuration.setDefaultScriptingLanguage(CustomScriptLanguageDriver.class);

        log.info("Mybatis: The interceptor has been added: " + SqlHelperMybatisPlugin.class);
        configuration.addInterceptor(sqlHelperSupplier.get());
    }
}
