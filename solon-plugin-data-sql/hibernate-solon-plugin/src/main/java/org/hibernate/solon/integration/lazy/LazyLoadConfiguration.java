package org.hibernate.solon.integration.lazy;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.Props;

/**
 * 懒加载配置类
 * 
 * <p>配置Hibernate的懒加载策略，避免N+1查询问题</p>
 * 
 * @author noear
 * @since 3.4
 */
@Configuration
public class LazyLoadConfiguration {
    
    /**
     * 配置懒加载设置
     */
    @Bean
    public void configureLazyLoading() {
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return;
        }
        
        // 禁用无事务时的懒加载（避免LazyInitializationException）
        if (!jpaProps.containsKey("properties.hibernate.enable_lazy_load_no_trans")) {
            Solon.cfg().put("jpa.properties.hibernate.enable_lazy_load_no_trans", "false");
        }
        
        // 配置默认的抓取策略（JOIN FETCH）
        if (!jpaProps.containsKey("properties.hibernate.default_batch_fetch_size")) {
            Solon.cfg().put("jpa.properties.hibernate.default_batch_fetch_size", "16");
        }
        
        // 配置子查询抓取
        if (!jpaProps.containsKey("properties.hibernate.use_subselect_fetch")) {
            Solon.cfg().put("jpa.properties.hibernate.use_subselect_fetch", "true");
        }
    }
}

