package org.apache.ibatis.solon.integration;

import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.data.datasource.DsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author noear
 * @since 3.0
 * //@deprecated 3.2 不再支持加在 Mapper 上（只能通过配置）
 */
//@Deprecated
public class DbBeanBuilderImpl extends DsBuilder<Db> {
    static final Logger log = LoggerFactory.getLogger(DbBeanBuilderImpl.class);

    public DbBeanBuilderImpl() {
        super(Db::value);

        addHandler(this::buildHandle);
    }

    public boolean buildHandle(Class<?> clz, BeanWrap dsBw) {
        if (clz.isInterface() == false) {
            log.warn("Db builder is no longer supported, class: {}", clz.getName());

            Object raw = MybatisAdapterManager.get(dsBw).getMapper(clz);
            dsBw.context().wrapAndPut(clz, raw);
            return true;
        }

        return false;
    }
}