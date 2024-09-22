package cn.dev33.satoken.solon.sso;

import cn.dev33.satoken.sso.SaSsoManager;
import cn.dev33.satoken.sso.config.SaSsoClientConfig;
import cn.dev33.satoken.sso.config.SaSsoServerConfig;
import cn.dev33.satoken.sso.processor.SaSsoClientProcessor;
import cn.dev33.satoken.sso.processor.SaSsoServerProcessor;
import cn.dev33.satoken.sso.template.SaSsoClientTemplate;
import cn.dev33.satoken.sso.template.SaSsoServerTemplate;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;

/**
 * @author noear
 * @since 2.0
 */

@Condition(onClass = SaSsoManager.class)
@Configuration
public class SaSsoAutoConfigure {
    @Bean
    public void init(AppContext appContext) throws Throwable {
        appContext.subBeansOfType(SaSsoServerTemplate.class, bean -> {
            SaSsoServerProcessor.instance.ssoServerTemplate = bean;
        });
        appContext.subBeansOfType(SaSsoClientTemplate.class, bean -> {
            SaSsoClientProcessor.instance.ssoClientTemplate = bean;
        });

        appContext.subBeansOfType(SaSsoServerConfig.class, bean -> {
            SaSsoManager.setServerConfig(bean);
        });
        appContext.subBeansOfType(SaSsoClientConfig.class, bean -> {
            SaSsoManager.setClientConfig(bean);
        });
    }

    /**
     * 获取 SSO Server 配置Bean
     */
    @Bean
    public SaSsoServerConfig getConfig(@Inject(value = "${sa-token.sso-server}", required = false) SaSsoServerConfig ssoConfig) {
        return ssoConfig;
    }

    /**
     * 获取 SSO Client 配置Bean
     */
    @Bean
    public SaSsoClientConfig getClientConfig(@Inject(value = "${sa-token.sso-client}", required = false) SaSsoClientConfig ssoConfig) {
        return ssoConfig;
    }
}