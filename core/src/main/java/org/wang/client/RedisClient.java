package org.wang.client;

import java.util.concurrent.CompletableFuture;

/**
 * @author wangjiabao
 */
public interface RedisClient {

    String getClientId();

    /**
     * publish message to channel
     *
     * @param channel
     * @param message
     * @return
     */
    long publish(String channel, String message);

    /**
     * subscribe channel
     *
     * @param channel
     * @return
     */
    CompletableFuture<Boolean> subscribe(String channel);

    Object executeLua(String script, String key, Object[] args);

}
