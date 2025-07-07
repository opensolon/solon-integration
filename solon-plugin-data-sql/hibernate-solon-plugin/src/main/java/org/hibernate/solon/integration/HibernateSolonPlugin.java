package org.hibernate.solon.integration;

import org.hibernate.solon.annotation.Db;
import org.noear.solon.Utils;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsInjector;
import org.noear.solon.data.datasource.DsUtils;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.sql.DataSource;

/**
 * @author noear
 * @since 2.5
 * @since 3.4
 */
public class HibernateSolonPlugin implements Plugin {

    @Override
    public void start(AppContext context) throws Throwable {
        //增加 jpa 的 solon yml 配置支持
        PersistenceProviderResolverHolder
                .getPersistenceProviderResolver()
                .getPersistenceProviders()
                .add(new JpaPersistenceProvider());

        context.subWrapsOfType(DataSource.class, HibernateAdapterManager::register);

        //添加 db 注入处理
        DbBeanInjectorImpl dbBeanInjector = new DbBeanInjectorImpl();
        context.beanInjectorAdd(Db.class, dbBeanInjector);
        DsInjector.getDefault().addHandler(dbBeanInjector::injectHandle);

        // 标准 jpa PersistenceContext 注入支持
        //PersistenceUnit
        context.beanInjectorAdd(PersistenceContext.class, this::persistenceContextInject);
        context.beanInjectorAdd(PersistenceUnit.class, this::persistenceUnitInject);
    }

    private void persistenceContextInject(VarHolder vh, PersistenceContext anno) {
        String unitName = Utils.annoAlias(anno.unitName(), anno.name());

        DsUtils.observeDs(vh.context(), unitName, dsBw -> {
            HibernateAdapter adapter = HibernateAdapterManager.get(dsBw);

            if (adapter != null) {
                adapter.injectTo(vh);
            }
        });
    }

    private void persistenceUnitInject(VarHolder vh, PersistenceUnit anno) {
        String unitName = Utils.annoAlias(anno.unitName(), anno.name());

        DsUtils.observeDs(vh.context(), unitName, dsBw -> {
            HibernateAdapter adapter = HibernateAdapterManager.get(dsBw);

            if (adapter != null) {
                adapter.injectTo(vh);
            }
        });
    }
}
