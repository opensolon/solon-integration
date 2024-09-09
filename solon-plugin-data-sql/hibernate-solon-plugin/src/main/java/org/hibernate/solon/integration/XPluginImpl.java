package org.hibernate.solon.integration;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.solon.annotation.Db;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;
import org.noear.solon.data.annotation.Ds;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.sql.DataSource;

/**
 * @author noear
 * @since 2.5
 */
public class XPluginImpl implements Plugin {

    @Override
    public void start(AppContext context) throws Throwable {
        //增加 jpa 的 solon yml 配置支持
        PersistenceProviderResolverHolder
                .getPersistenceProviderResolver()
                .getPersistenceProviders()
                .add(new JpaPersistenceProvider());

        context.subWrapsOfType(DataSource.class, HibernateAdapterManager::register);

        context.beanInjectorAdd(Db.class, new DbBeanInjector());

        //@since 2.9
        DsBeanInjectorImpl injector = new DsBeanInjectorImpl();
        context.beanInjectorAdd(Ds.class, SessionFactory.class, injector);
        context.beanInjectorAdd(Ds.class, Configuration.class, injector);
        context.beanInjectorAdd(Ds.class, EntityManagerFactory.class, injector);
    }
}
