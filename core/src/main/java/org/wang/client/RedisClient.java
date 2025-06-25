package org.wang.client;

/**
 * @author wangjiabao
 */
public interface RedisClient {

    String getClientId();

    Object executeLua(String script, String key, Object[] args);

}
