package org.hibernate.solon.integration;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author noear 2025/12/16 created
 * @since 3.7.4
 */
public class HibernateSessionInterceptor implements InvocationHandler {
    private SessionFactory sessionFactory;

    public HibernateSessionInterceptor(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try (Session sqlSession = sessionFactory.openSession()) {
            return method.invoke(sqlSession, args);
        }
    }
}
