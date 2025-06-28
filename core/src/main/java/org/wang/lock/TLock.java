package org.wang.lock;

import java.util.concurrent.TimeUnit;

/**
 * Tiny Distributed lock
 *
 * @author wangjiabao
 */
public interface TLock {

    String getName();

    /**
     * Attempts to acquire the lock; the thread will block until the lock is acquired.
     */
    void lock();

    /**
     * Attempts to acquire the lock; the thread will block until the lock is acquired.
     * If an interrupt signal is received during the waiting period, an InterruptedException is thrown.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    void lockInterruptibly() throws InterruptedException;

    /**
     * Releases the lock.
     */
    void unlock();

    /**
     * Attempts to acquire the lock and returns the result of the acquisition.
     *
     * @param leaseTime Duration for which the lock is held.
     * @param unit      Time unit for the lease time.
     * @return true if the lock was acquired, false otherwise.
     */
    boolean tryLock(long leaseTime, TimeUnit unit);

    /**
     * Attempts to acquire the lock for a duration of leaseTime.
     * 1. If successful, returns immediately; if unsuccessful, waits for waitTime.
     * 2. If the lock is not acquired within waitTime, returns false.
     *
     * @param waitTime  Wait time upon failure to acquire the lock.
     * @param leaseTime Duration for which the lock is held.
     * @param unit      Time unit for both wait time and lease time.
     * @return true if the lock was acquired, false otherwise.
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit unit);

    /**
     * Attempts to acquire the lock for a duration of leaseTime and returns the result of the acquisition.
     * If an interrupt signal is received during the waiting period, an InterruptedException is thrown.
     *
     * @param waitTime  Wait time upon failure to acquire the lock.
     * @param leaseTime Duration for which the lock is held.
     * @param unit      Time unit for both wait time and lease time.
     * @return true if the lock was acquired, false otherwise.
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    boolean tryLockInterruptibly(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException;
}