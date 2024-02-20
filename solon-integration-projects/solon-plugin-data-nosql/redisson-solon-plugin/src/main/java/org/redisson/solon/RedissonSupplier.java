package org.redisson.solon;

import org.noear.solon.Utils;
import org.noear.solon.core.util.ResourceUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author noear
 * @since 2.2
 */
public class RedissonSupplier implements Supplier<RedissonClient> {
    private Properties properties;

    private Consumer<Config> configUpdater;

    public RedissonSupplier(Properties properties) {
        this.properties = properties;
    }

    public RedissonSupplier withConfigUpdater(Consumer<Config> configUpdater){
        this.configUpdater = configUpdater;
        return this;
    }

    @Override
    public RedissonClient get() {
        try {
            String fileUri = properties.getProperty("file");
            if (Utils.isNotEmpty(fileUri)) {
                URL url = ResourceUtil.findResource(fileUri);
                Config config = Config.fromYAML(url);

                Optional.ofNullable(configUpdater).ifPresent(configConsumer -> configConsumer.accept(config));

                return Redisson.create(config);

            }

            String configTxt = properties.getProperty("config");
            if (Utils.isNotEmpty(configTxt)) {
                Config config = Config.fromYAML(configTxt);

                Optional.ofNullable(configUpdater).ifPresent(configConsumer -> configConsumer.accept(config));

                return Redisson.create(config);
            }
        } catch (Exception e) {
            throw new IllegalStateException("The redisson configuration failed", e);
        }

        throw new IllegalStateException("Invalid redisson configuration");
    }
}
