package net.hasor.dbvisitor.solon.integration;

import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.dbvisitor.solon.annotation.Db;
import org.noear.solon.core.*;

/**
 * @author noear
 * @since 1.8
 */
public class XPluginImp implements Plugin {
    private DalRegistry dalRegistry = new DalRegistry();

    @Override
    public void start(AppContext context) {

        //添加 db 注入处理
        context.beanInjectorAdd(Db.class, new DbBeanInjectorImpl(dalRegistry));

        context.beanBuilderAdd(RefMapper.class, (clz, bw, anno) -> {
            dalRegistry.loadMapper(clz);
        });
    }
}
