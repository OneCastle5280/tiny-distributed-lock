package org.wang.client;

/**
 * @author wangjiabao
 */
public interface RedisClient {

    Object executeLua(String script, String key, Object[] args);

}
