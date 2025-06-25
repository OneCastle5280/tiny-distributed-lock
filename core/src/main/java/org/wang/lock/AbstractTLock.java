package org.wang.lock;

import org.wang.client.RedisClient;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author wangjiabao
 */
public abstract class AbstractTLock implements TLock{

    protected String name;

    protected String clientId;
    protected RedisClient redisClient;

    protected AbstractTLock(String name) {
        this.name = name;
        // TODO init redisClient
        this.clientId = this.redisClient.getClientId();
    }

    public String getName() {
        return this.name;
    }

    public void lock() {
        lock0(DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT);
    }

    public boolean tryLock(long leaseTime, TimeUnit unit) throws InterruptedException {
        return tryAcquire(leaseTime, unit) == null;
    }

    public void unlock() {
        unlock0();
    }

    protected void unlock0() {
        String unlockLua =
                "if redis.call('EXISTS', KEYS[1]) == 1 then " +
                        "    return redis.call('DEL', KEYS[1]) " +
                        "else " +
                        "    return nil " +
                        "end";
        Object result = this.redisClient.executeLua(unlockLua, getLockKey(), null);
        if (result == null) {
            throw new IllegalStateException("illegal operator, " + getLockKey() + "is not hold the lock");
        }
        // return 0 or 1 means unlock success
        // TODO notice others waiting thread to acquire lock
    }


    protected void lock0(long waitTime, long leaseTime, TimeUnit unit) {
        // try to acquire lock
        Long ttl = tryAcquire(leaseTime, unit);
        if (ttl == null) {
            // acquire success
            return;
        }

        // ttl > 0, thread need wait lock released
        // TODO 等待机制
    }

    /**
     * if acquire success, return 0; else return ttl
     *
     * @param leaseTime
     * @param unit
     * @return
     */
    protected Long tryAcquire(long leaseTime, TimeUnit unit) {
        String tryLockLua =
                "if redis.call('SETNX', KEYS[1], '1']) == '1' then " +
                        "    redis.call('PEXPIRE', KEYS[1], ARGV[1]) " +
                        "    return nil" +
                        "else" +
                        "    return redis.call('PTTL', KEYS[1])" +
                        "end"
                ;

        Object[] args = new Object[1];
        args[0] = unit.toMillis(leaseTime);
        Object ttl = this.redisClient.executeLua(tryLockLua, getLockKey(), args);
        if (ttl == null) {
            // acquire lock success, add watchdog to renewal
            // TODO watchdog
            return 0L;
        }
        return (Long) ttl;
    }

    /**
     * get lock key, which is thread id + client id
     *
     * @return
     */
    protected String getLockKey() {
        return String.valueOf(Thread.currentThread().getId()) + ":" + this.clientId;
    }
}
