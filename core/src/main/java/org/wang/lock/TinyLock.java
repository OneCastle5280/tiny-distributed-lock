package org.wang.lock;

/**
 * Define the behavior of lock
 *
 * @author wangjiabao
 */
public interface TinyLock {

    boolean lock(String key);

    boolean unlock(String key);
}
