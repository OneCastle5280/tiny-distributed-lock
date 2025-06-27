package org.wang.client;

import org.wang.exception.JedisConnectException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @author wangjiabao
 */
public class JedisRedisClient implements RedisClient {

    private Jedis jedis;

    public JedisRedisClient(InetSocketAddress socketAddress) throws JedisConnectException {
        try {
            jedis = new Jedis(socketAddress.getHostName(), socketAddress.getPort());
        } catch (Exception e) {
            // connect error
            throw new JedisConnectException();
        }
    }

    @Override
    public String getClientId() {
        return String.valueOf(jedis.clientId());
    }

    @Override
    public Object executeLua(String script, String key, Object[] args) {

        // TODO
        return null;
    }

    @Override
    public long publish(String channel, String message) {
        return 0;
    }

    @Override
    public CompletableFuture<Boolean> subscribe(String channel) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                // TODO
            }
        };
        jedis.subscribe(jedisPubSub, channel)
    }
}
