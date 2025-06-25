package org.wang.client;

import org.wang.exception.JedisConnectException;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;

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

    public Object executeLua(String script, String key, Object[] args) {
        // TODO
        return null;
    }
}
