package org.hibernate.solon.integration.audit;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.Props;

/**
 * 审计配置类
 * 
 * <p>配置审计监听器</p>
 * 
 * @author noear
 * @since 3.4
 */
@Configuration
public class AuditConfiguration {
    
    /**
     * 配置审计监听器
     */
    @Bean
    public void configureAudit() {
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return;
        }
        
        // 检查是否启用审计功能
        boolean enableAudit = jpaProps.getBool("audit.enabled", true);
        
        if (enableAudit) {
            // 注册审计监听器
            // 注意：这里需要在HibernateConfiguration中注册事件监听器
            // 由于Hibernate的配置方式，这里只是标记配置
            Solon.cfg().put("jpa.audit.enabled", "true");
        }
    }
}

