package org.wang.client;

import org.wang.exception.JedisConnectException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author wangjiabao
 */
public class JedisRedisClient implements RedisClient {

    private final Jedis jedis;

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
    public Object executeLua(String script, String key, List<String> args) {
        List<String> keys = new ArrayList<>();
        keys.add(key);

        return jedis.eval(script, keys, args);
    }

    @Override
    public long publish(String channel, String message) {
        return jedis.publish(channel, message);
    }

    @Override
    public CompletableFuture<Boolean> subscribe(String ch) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (ch.equals(channel) && "1".equals(message)) {
                    result.complete(Boolean.TRUE);
                }
            }
        }, ch);
        return result;
    }

    @Override
    public void unSubscribe(String channel) {
        new JedisPubSub() {
            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                // TODO log
            }
        }.unsubscribe(channel);
    }
}
