package org.wang.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author wangjiabao
 */
public interface RedisClient {

    /**
     * @return return the client id
     */
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

    /**
     * unsubscribe channel
     *
     * @param channel
     * @return
     */
    void unSubscribe(String channel);

    Object executeLua(String script, String key, List<String> args);

}
