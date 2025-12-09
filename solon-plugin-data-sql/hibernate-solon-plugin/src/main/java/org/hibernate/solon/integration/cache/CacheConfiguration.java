package org.hibernate.solon.integration.cache;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.Props;

/**
 * 缓存配置类
 * 
 * <p>配置Hibernate的二级缓存和查询缓存</p>
 * 
 * @author noear
 * @since 3.4
 */
@Configuration
public class CacheConfiguration {
    
    /**
     * 配置缓存设置
     */
    @Bean
    public void configureCache() {
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return;
        }
        
        // 检查是否启用二级缓存
        boolean useSecondLevelCache = jpaProps.getBool("properties.hibernate.cache.use_second_level_cache", false);
        
        if (useSecondLevelCache) {
            // 启用二级缓存
            if (!jpaProps.containsKey("properties.hibernate.cache.use_second_level_cache")) {
                Solon.cfg().put("jpa.properties.hibernate.cache.use_second_level_cache", "true");
            }
            
            // 启用查询缓存
            boolean useQueryCache = jpaProps.getBool("properties.hibernate.cache.use_query_cache", true);
            if (!jpaProps.containsKey("properties.hibernate.cache.use_query_cache")) {
                Solon.cfg().put("jpa.properties.hibernate.cache.use_query_cache", String.valueOf(useQueryCache));
            }
            
            // 配置缓存提供者（如果没有指定，使用默认的）
            if (!jpaProps.containsKey("properties.hibernate.cache.region.factory_class")) {
                // 尝试使用EhCache
                try {
                    Class.forName("org.hibernate.cache.ehcache.EhCacheRegionFactory");
                    Solon.cfg().put("jpa.properties.hibernate.cache.region.factory_class", 
                        "org.hibernate.cache.ehcache.EhCacheRegionFactory");
                } catch (ClassNotFoundException e) {
                    // 如果EhCache不可用，使用默认的SimpleCacheProvider
                    Solon.cfg().put("jpa.properties.hibernate.cache.region.factory_class", 
                        "org.hibernate.cache.internal.SimpleCacheProvider");
                }
            }
        }
    }
}

