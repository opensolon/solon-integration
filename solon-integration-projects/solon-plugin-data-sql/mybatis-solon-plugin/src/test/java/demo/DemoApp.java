package demo;

import org.apache.ibatis.session.Configuration;
import org.noear.solon.Solon;
import org.noear.solon.core.event.AppPluginLoadEndEvent;

/**
 * @author noear 2021/7/12 created
 */
public class DemoApp {
    public static void main(String[] args) {
        Solon.start(DemoApp.class, args, app -> {
            app.onEvent(Configuration.class, c -> {
                //添加插件
                //c.addInterceptor();
            });

            app.onEvent(AppPluginLoadEndEvent.class, e -> {
                //重新定义 SqlSessionFactoryBuilder（没有需要，最好别动它...）
                //Solon.context().wrapAndPut(SqlSessionFactoryBuilder.class, new SqlSessionFactoryBuilderImpl());
            });
        });
    }
}
