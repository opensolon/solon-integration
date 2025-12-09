package org.hibernate.solon.integration.batch;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.Props;

/**
 * 批量操作配置类
 * 
 * <p>自动配置Hibernate的批量操作参数</p>
 * 
 * @author noear
 * @since 3.4
 */
@Configuration
public class BatchConfiguration {
    
    /**
     * 配置批量操作参数
     */
    @Bean
    public void configureBatchSettings() {
        Props jpaProps = Solon.cfg().getProp("jpa");
        if (jpaProps == null) {
            return;
        }
        
        // 获取批量大小配置，默认50
        int batchSize = jpaProps.getInt("properties.hibernate.jdbc.batch_size", 50);
        
        // 如果配置文件中没有设置，则设置默认值
        if (!jpaProps.containsKey("properties.hibernate.jdbc.batch_size")) {
            Solon.cfg().put("jpa.properties.hibernate.jdbc.batch_size", String.valueOf(batchSize));
        }
        
        // 启用批量版本化数据
        if (!jpaProps.containsKey("properties.hibernate.jdbc.batch_versioned_data")) {
            Solon.cfg().put("jpa.properties.hibernate.jdbc.batch_versioned_data", "true");
        }
        
        // 设置批量顺序插入
        if (!jpaProps.containsKey("properties.hibernate.order_inserts")) {
            Solon.cfg().put("jpa.properties.hibernate.order_inserts", "true");
        }
        
        // 设置批量顺序更新
        if (!jpaProps.containsKey("properties.hibernate.order_updates")) {
            Solon.cfg().put("jpa.properties.hibernate.order_updates", "true");
        }
    }
}

