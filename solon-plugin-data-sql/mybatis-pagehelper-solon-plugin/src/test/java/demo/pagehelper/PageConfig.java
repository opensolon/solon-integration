package demo.pagehelper;

import com.github.pagehelper.PageInterceptor;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;

import java.util.Properties;

/**
 * @author noear 2025/7/4 created
 */
@Configuration
public class PageConfig {
    @Bean
    public PageInterceptor pageInterceptor(@Inject("${pagehelper}") Properties pageProps) {
        PageInterceptor pageInterceptor = new PageInterceptor();
        pageInterceptor.setProperties(pageProps);
        return pageInterceptor;
    }

    @Bean
    public void db1(@Ds("db1") org.apache.ibatis.session.Configuration cfg,
                    PageInterceptor pageInterceptor) {
        cfg.addInterceptor(pageInterceptor);
    }
}