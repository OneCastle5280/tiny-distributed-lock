package org.wang.lock;

import org.wang.client.RedisClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.wang.constants.TLcokConstants.*;

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

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void lock() {
        tryAcquire(DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT);
    }

    @Override
    public boolean tryLock(long leaseTime, TimeUnit unit) throws InterruptedException {
        return tryAcquire0(leaseTime, unit) == null;
    }

    @Override
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


    protected boolean tryAcquire(long waitTime, long leaseTime, TimeUnit unit) {
        long waitTimeMillis = unit.toMillis(waitTime);
        long current = System.currentTimeMillis();

        // try to acquire lock
        Long ttl = tryAcquire0(leaseTime, unit);
        if (ttl == null) {
            // acquire success
            return true;
        }

        long remainWaitTime = waitTimeMillis - System.currentTimeMillis() - current;
        if (remainWaitTime <= 0) {
            // had over waitTime, return fail
            return false;
        }
        // ttl > 0 && remainWaitTime > 0, thread need wait lock released
        CompletableFuture<Boolean> future = subscribe();
        future.whenComplete((r, e) -> {
            if (e != null) {
                // TODO subscribe has exception
            }
            if (r) {
                // try to acquire PubSubLock, if success, try to acquire TLock
                if (PubSubLock.tryLock()) {
                    while (true) {
                        // try to acquire lock again
                        Long lockTtl = tryAcquire0(leaseTime, unit);
                        if (lockTtl == null) {
                            // success

                        }
                    }
                }
            }
        });

    }

    /**
     * if acquire success, return 0; else return ttl
     *
     * @param leaseTime
     * @param unit
     * @return
     */
    protected Long tryAcquire0(long leaseTime, TimeUnit unit) {
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
     * @return lock key
     */
    protected String getLockKey() {
        return Thread.currentThread().getId() + ":" + this.clientId;
    }

    protected String getChannelName() {
        return "TLock__channel" + this.name;
    }

    protected CompletableFuture<Boolean> subscribe() {
        return this.redisClient.subscribe(getChannelName());
    }
}
