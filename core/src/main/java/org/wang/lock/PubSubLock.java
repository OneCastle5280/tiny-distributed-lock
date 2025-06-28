package org.wang.lock;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * When lock competition occurs, need to acquire the lock first to obtain the TLock
 *
 * @author wangjiabao
 */
public class PubSubLock {

    private final Semaphore lock;

    public PubSubLock() {
        this.lock = new Semaphore(0);
    }

    public void tryLock(long time, TimeUnit unit) throws InterruptedException {
        lock.tryAcquire(time, unit);
    }

    public void unLock() {
        this.lock.release(1);
    }
}
