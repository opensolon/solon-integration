package com.jn.sqlhelper.mybatis.solon.integration;

import com.jn.sqlhelper.dialect.instrument.SQLInstrumentorConfig;
import com.jn.sqlhelper.mybatis.plugins.SqlHelperMybatisPlugin;
import com.jn.sqlhelper.mybatis.plugins.pagination.PaginationConfig;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.Props;

import java.util.function.Supplier;

/**
 * 分页插件提供者
 *
 * @author noear
 * @since 3.0
 */
public class SqlHelperSupplier implements Supplier<SqlHelperMybatisPlugin> {
    private final SQLInstrumentorConfig sqlInstrumentConfig;
    private final PaginationConfig paginationPluginConfig;
    private SqlHelperMybatisPlugin pageInterceptor;

    public SqlHelperSupplier() {
        this(null);
    }

    public SqlHelperSupplier(String propStarts) {
        if (Utils.isEmpty(propStarts)) {
            propStarts = "sqlhelper.mybatis";
        }

        Props props = Solon.cfg().getProp(propStarts);
        paginationPluginConfig = props.toBean("pagination", PaginationConfig.class);
        sqlInstrumentConfig = props.toBean("instrumentor", SQLInstrumentorConfig.class);
    }

    /**
     * 分页配置
     */
    public PaginationConfig paginationConfig() {
        return paginationPluginConfig;
    }

    /**
     * 工具配置
     */
    public SQLInstrumentorConfig instrumentConfig() {
        return sqlInstrumentConfig;
    }

    /**
     * 获取
     */
    @Override
    public SqlHelperMybatisPlugin get() {
        if (paginationPluginConfig == null || sqlInstrumentConfig == null) {
            return null;
        }

        if (pageInterceptor == null) {
            pageInterceptor = new SqlHelperMybatisPlugin();
            pageInterceptor.setPaginationConfig(paginationPluginConfig);
            pageInterceptor.setInstrumentorConfig(sqlInstrumentConfig);
            pageInterceptor.init();
        }

        return pageInterceptor;
    }
}
