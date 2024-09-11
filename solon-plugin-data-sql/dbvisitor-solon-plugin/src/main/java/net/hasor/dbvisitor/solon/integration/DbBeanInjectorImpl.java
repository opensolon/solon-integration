package net.hasor.dbvisitor.solon.integration;

import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.solon.annotation.Db;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.VarHolder;
import org.noear.solon.data.datasource.DsUtils;

import javax.sql.DataSource;

/**
 * @author noear
 * @since 2.9
 */
public class DbBeanInjectorImpl implements BeanInjector<Db> {
    private final DalRegistry dalRegistry;
    public DbBeanInjectorImpl(DalRegistry dalRegistry) {
        this.dalRegistry = dalRegistry;
    }

    @Override
    public void doInject(VarHolder vh, Db anno) {
        DsUtils.observeDs(vh.context(), anno.value(), (dsWrap) -> {
            inject0(vh, dsWrap);
        });
    }

    private void inject0(VarHolder varH, BeanWrap dsBw) {
        try {
            inject1(varH, dsBw);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void inject1(VarHolder varH, BeanWrap dsBw) throws Exception {
        DataSource ds = dsBw.get();
        Class<?> clz = varH.getType();

        //@Db("db1") LambdaTemplate ; //顺序别乱变
        if (LambdaTemplate.class.isAssignableFrom(varH.getType())) {
            LambdaTemplate accessor = new LambdaTemplate(new SolonManagedDynamicConnection(ds));

            varH.setValue(accessor);
            return;
        }

        //@Db("db1") JdbcTemplate ;
        if (JdbcTemplate.class.isAssignableFrom(varH.getType())) {
            JdbcTemplate accessor = new JdbcTemplate(new SolonManagedDynamicConnection(ds));

            varH.setValue(accessor);
            return;
        }

        //@Db("db1") DalSession ;
        if (DalSession.class.isAssignableFrom(varH.getType())) {
            DalSession accessor = new DalSession(new SolonManagedDynamicConnection(ds));

            varH.setValue(accessor);
            return;
        }

        //@Db("db1") UserMapper ;
        if (varH.getType().isInterface()) {
            DalSession accessor = new DalSession(new SolonManagedDynamicConnection(ds), dalRegistry);

            if (clz == BaseMapper.class) {
                Object obj = accessor.createBaseMapper((Class<?>) varH.getGenericType().getActualTypeArguments()[0]);
                varH.setValue(obj);
            } else {
                Object mapper = accessor.createMapper(varH.getType());
                varH.setValue(mapper);
            }
            return;
        }
    }
}
