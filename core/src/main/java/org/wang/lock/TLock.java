package org.wang.lock;

import java.util.concurrent.TimeUnit;

/**
 * Tiny Distributed lock
 *
 * @author wangjiabao
 */
public interface TLock {

    String getName();

    void lock();

    void lockInterruptibly() throws InterruptedException;

    void unlock();

    boolean tryLock(long leaseTime, TimeUnit unit);

    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit);

    boolean tryLockInterruptibly(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;
}
