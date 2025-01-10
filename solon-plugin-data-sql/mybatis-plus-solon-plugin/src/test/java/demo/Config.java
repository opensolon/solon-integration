package demo;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import demo.dso.MetaObjectHandlerImpl;
import demo.dso.MybatisSqlSessionFactoryBuilderImpl;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.util.ResourceUtil;
import org.noear.solon.data.sql.SqlUtils;

@Configuration
public class Config {
    @Bean
    public void db1(@Inject SqlUtils sqlUtils) throws Exception {
        String sql = ResourceUtil.getResourceAsString("db.sql");

        for (String s1 : sql.split(";")) {
            if (s1.trim().length() > 10) {
                sqlUtils.sql(s1).update();
            }
        }
    }

//    @Bean
//    public Interceptor plusInterceptor() {
//        MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
//        plusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
//        return plusInterceptor;
//    }

    @Bean
    public void db1_ext(@Db("db1") GlobalConfig globalConfig) {
        MetaObjectHandler metaObjectHandler = new MetaObjectHandlerImpl();

        globalConfig.setMetaObjectHandler(metaObjectHandler);
    }

    @Bean
    public void db1_ext2(@Db("db1") MybatisConfiguration config){
//        config.getTypeHandlerRegistry().register("xxx");
//        config.setDefaultEnumTypeHandler(null);
    }

    @Bean
    public MybatisSqlSessionFactoryBuilder factoryBuilderNew(){
        return new MybatisSqlSessionFactoryBuilderImpl();
    }
}
