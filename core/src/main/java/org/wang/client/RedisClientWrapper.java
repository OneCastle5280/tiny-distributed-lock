package org.wang.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * wrap {@link RedisClient}
 *
 * @author wangjiabao
 */
public class RedisClientWrapper implements RedisClient{

    private RedisClient redisClient;

    public RedisClientWrapper() {
        // TODO SPI redisClient
        redisClient = new JedisRedisClient(new InetSocketAddress("127.0.0.1", 6379));
    }

    @Override
    public Object executeLua(String script, String key, List<String> args) {
        return this.redisClient.executeLua(script, key, args);
    }

    @Override
    public String getClientId() {
        return this.redisClient.getClientId();
    }

    @Override
    public long publish(String channel, String message) {
        return this.redisClient.publish(channel, message);
    }

    @Override
    public CompletableFuture<Boolean> subscribe(String channel) {
        return this.redisClient.subscribe(channel);
    }

    @Override
    public void unSubscribe(String channel) {
        this.redisClient.unSubscribe(channel);
    }
}
