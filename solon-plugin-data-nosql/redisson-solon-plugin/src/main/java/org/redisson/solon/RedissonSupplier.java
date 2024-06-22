package org.redisson.solon;

import java.util.Properties;

/**
 * RedissonClient 提供者（使用原生配置）
 *
 * @author noear
 * @since 2.2
 * @deprecated 2.8
 */
@Deprecated
public class RedissonSupplier extends RedissonClientOriginalSupplier {
    public RedissonSupplier(Properties properties) {
        super(properties);
    }
}