package org.hibernate.solon.test.demo;

import org.hibernate.solon.annotation.Db;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.data.annotation.Ds;
import org.noear.solon.data.annotation.Tran;
import org.noear.solon.data.annotation.Transaction;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@Mapping("jpa")
@Controller
public class JapController {
    @Ds
    private EntityManagerFactory sessionFactory;

    private EntityManager openSession() {
        return sessionFactory.createEntityManager();
    }

    @Transaction
    @Mapping("/t")
    public void t1() {
        HttpEntity entity = new HttpEntity();
        entity.setId(System.currentTimeMillis() + "");

        openSession().persist(entity);

    }

    @Mapping("/t2")
    public Object t2() {
        HttpEntity entity = new HttpEntity();
        entity.setId(System.currentTimeMillis() + "");

        return openSession().find(HttpEntity.class, "1");
    }

    @Mapping("/t3")
    public Object t3() {
        return openSession()
                .createQuery("select e from HttpEntity e")
                .setMaxResults(1)
                .getResultList();
    }
}