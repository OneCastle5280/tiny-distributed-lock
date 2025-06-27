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

    /**
     * try to acquire lock, if timeout will return false
     *
     * @param time
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return lock.tryAcquire(time, unit);
    }

    /**
     * will block until the lock is successfully acquired
     */
    public void acquireLock() throws InterruptedException{
        this.lock.acquire();
    }

    /**
     * release lock
     */
    public void unLock() {
        this.lock.release(1);
    }
}
