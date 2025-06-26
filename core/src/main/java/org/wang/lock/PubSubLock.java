package org.wang.lock;

import java.util.concurrent.Semaphore;

/**
 * When lock competition occurs, need to acquire the lock first to obtain the TLock
 *
 * @author wangjiabao
 */
public class PubSubLock {

    private final static Semaphore lock = new Semaphore(1);

    public static boolean tryLock() {
        return lock.tryAcquire();
    }

    public static void release() {
        lock.release();
    }
}
