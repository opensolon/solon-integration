package org.hibernate.solon.integration;

import org.hibernate.cfg.AvailableSettings;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vanlin
 * @className PersistenceContextBeanInjector
 * @description
 * @since 2024/6/28 10:07
 */
public class PersistenceContextBeanInjector implements BeanInjector<PersistenceContext> {
    private final ConcurrentHashMap<String, EntityManagerFactory> ENTITY_MANAGER_FACTORIES = new ConcurrentHashMap<>();


    @Override
    public void doInject(VarHolder varH, PersistenceContext anno) {
        String unitName = Utils.annoAlias(anno.unitName(), anno.name());

        DsUtils.observeDs(varH.context(), unitName, dsBw -> {
            inject0(anno, varH, dsBw);
        });
    }

    private void inject0(PersistenceContext anno, VarHolder varH, BeanWrap dsBw) {
        final Props dsProps = Solon.cfg().getProp("solon.jpa." + dsBw.name());
        final Props properties = dsProps.getProp("properties");

        properties.put(AvailableSettings.DATASOURCE, dsBw.raw());

        final EntityManagerFactory entityManagerFactory = ENTITY_MANAGER_FACTORIES.computeIfAbsent(dsBw.name(),
                key -> Persistence.createEntityManagerFactory(key, properties));

        if (EntityManager.class.equals(varH.getType())) {
            varH.setValue(new EntityManagerProxy(entityManagerFactory.createEntityManager()));
        } else if (EntityManagerFactory.class.equals(varH.getType())) {
            varH.setValue(entityManagerFactory);
        }
    }
}