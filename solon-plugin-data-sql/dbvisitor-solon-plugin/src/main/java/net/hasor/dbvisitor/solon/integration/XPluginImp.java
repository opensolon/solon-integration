package net.hasor.dbvisitor.solon.integration;

import net.hasor.dbvisitor.dal.mapper.Mapper;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.solon.annotation.Mappable;
import org.noear.solon.core.*;
import org.noear.solon.data.annotation.Ds;

/**
 * @author noear
 * @since 1.8
 */
public class XPluginImp implements Plugin {
    private DalRegistry dalRegistry = new DalRegistry();

    @Override
    public void start(AppContext context) {
        DsBeanInjectorImpl injector = new DsBeanInjectorImpl(dalRegistry);

        context.beanInjectorAdd(Ds.class, LambdaTemplate.class, injector);
        context.beanInjectorAdd(Ds.class, JdbcTemplate.class, injector);
        context.beanInjectorAdd(Ds.class, DalSession.class, injector);

        context.beanInjectorAdd(Ds.class, Mapper.class, injector);
        context.beanInjectorAdd(Ds.class, Mappable.class, injector);

        context.beanBuilderAdd(RefMapper.class, (clz, bw, anno) -> {
            dalRegistry.loadMapper(clz);
        });
    }
}
