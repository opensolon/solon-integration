package io.lettuce.solon;

/**
 * Lettuce 属性
 *
 * @author Sorghum
 * @since 2.4
 */
public class LettuceProperties {

    /**
     * Redis模式 (standalone, cluster, sentinel)
     */
    private String redisMode;

    /**
     * RedisURI
     */
    private String redisUri;

    /**
     * Lettuce配置
     */
    private LettuceConfig config;

    public String getRedisMode() {
        return redisMode;
    }

    public void setRedisMode(String redisMode) {
        this.redisMode = redisMode;
    }

    public String getRedisUri() {
        return redisUri;
    }

    public void setRedisUri(String redisUri) {
        this.redisUri = redisUri;
    }

    public LettuceConfig getConfig() {
        return config;
    }

    public void setConfig(LettuceConfig config) {
        this.config = config;
    }
}