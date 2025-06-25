package org.wang.lock;

import org.wang.client.RedisClient;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author wangjiabao
 */
public abstract class AbstractTLock implements TLock{

    protected String name;

    protected RedisClient redisClient;

    protected AbstractTLock(String name) {
        this.name = name;
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
        String key = String.valueOf(Thread.currentThread().getId());

        String unlockLua =
                "if redis.call('GET', KEYS[1]) == ARGV[1] then" +
                        "    return redis.call('DEL', KEYS[1]) " +
                        "end " +
                        "return nil";
        this.redisClient.executeLua(unlockLua,)
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
        String key = String.valueOf(Thread.currentThread().getId());
        String uuid = UUID.randomUUID().toString();

        String tryLockLua =
                "if redis.call('SETNX', KEYS[1], ARGV[1]) == 1 then " +
                        "    redis.call('PEXPIRE', KEYS[1], ARGV[2]) " +
                        "    return nil" +
                        "else" +
                        "    return redis.call('PTTL', KEYS[1])" +
                        "end"
                ;

        Object[] args = new Object[2];
        args[0] = uuid;
        args[1] = unit.toMillis(leaseTime);
        // execute lua
        Object ttl = this.redisClient.executeLua(tryLockLua, key, args);
        if (ttl == null) {
            // acquire lock success, add watchdog to renewal
            // TODO watchdog
            return 0L;
        }
        return (Long) ttl;
    }

    public boolean tryLock() {
        return false;
    }


}
