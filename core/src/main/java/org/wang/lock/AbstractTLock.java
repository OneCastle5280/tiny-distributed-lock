package org.wang.lock;

import org.wang.client.RedisClientWrapper;
import org.wang.exception.SubscribeChannelException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.wang.constants.TLockConstants.DEFAULT_LEASE_TIME;
import static org.wang.constants.TLockConstants.DEFAULT_TIME_UNIT;

/**
 * @author wangjiabao
 */
public abstract class AbstractTLock implements TLock{

    protected String name;
    protected String clientId;
    protected RedisClientWrapper redisClientWrapper;
    protected PubSubLock pubSubLock;

    protected AbstractTLock(String name) {
        this.redisClientWrapper = new RedisClientWrapper();
        this.pubSubLock = new PubSubLock();

        this.name = name;
        this.clientId = this.redisClientWrapper.getClientId();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void lock() {
        try {
            tryAcquireTLock(-1, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT, false);
        } catch (InterruptedException e) {
            // Theoretically, no interrupt exception is returned here
            Thread.currentThread().interrupt();
            throw new IllegalStateException(getLockKey() + " lock fail");
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        tryAcquireTLock(-1, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT, true);
    }

    @Override
    public void unlock() {
        unlock0();
    }

    protected void unlock0() {
        /*
          check key is exist
            a. if exists, del key and publish 1 to channel, return result
            b. if not exists, return nil
         */
        String unlockLua =
                "if redis.call('EXISTS', KEYS[1]) == 1 then" +
                        "    local result = redis.call('DEL', KEYS[1])" +
                        "    redis.call('PUBLISH', ARGV[1], '1')" +
                        "    return result" +
                        "else" +
                        "    return nil" +
                        "end";

        List<String> args = new ArrayList<>();
        args.add(getChannel());

        Object result = this.redisClientWrapper.executeLua(unlockLua, getLockKey(), args);
        if (result == null) {
            throw new IllegalStateException("illegal operator, " + getLockKey() + "is not hold the lock");
        }
        // result not null means unlock success, notice others waiting thread to acquire lock
        this.pubSubLock.unLock();
    }

    @Override
    public boolean tryLock(long leaseTime, TimeUnit unit) {
        try {
            return tryAcquireTLock(-1, leaseTime, unit, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(getLockKey() + " try lock fail");
        }
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return tryAcquireTLock(waitTime, leaseTime, unit, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(getLockKey() + " try lock fail");
        }
    }

    @Override
    public boolean tryLockInterruptibly(long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        return tryAcquireTLock(waitTime, leaseTime, unit, true);
    }

    /**
     * try acquire TLock
     *
     * @param waitTime        If the lock is occupied by other threads, how long is allowed to wait, default -1, means not to wait
     * @param leaseTime       lease lock time
     * @param unit            waitTime,leaseTime time unit
     * @param interruptibly   whether throws {@link InterruptedException}, true: throw, false: not throw
     * @return
     * @throws InterruptedException
     */
    protected boolean tryAcquireTLock(long waitTime, long leaseTime, TimeUnit unit, boolean interruptibly) throws InterruptedException {
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
        // TODO timeout
        Boolean subscribeRes;
        try {
            subscribeRes = subscribe(remainWaitTime, unit, interruptibly);
        } catch (TimeoutException | ExecutionException e) {
            throw new SubscribeChannelException(e);
        }

        if (!subscribeRes) {
            // try to subscribe channel fail, return fail
            return false;
        }

        current = System.currentTimeMillis();
        try {
            // Spin lock
            while (true) {
                // try to acquire lock again
                ttl = tryAcquire0(leaseTime, unit);
                if (ttl == null) {
                    // acquire lock success, break
                    return true;
                } else {
                    if (ttl < 0) {
                        throw new IllegalStateException(getLockKey() + " ttl < 0");
                    } else {
                        // if remainWaitTime <= 0, return false
                        remainWaitTime -= System.currentTimeMillis() - current;
                        if (remainWaitTime <= 0) {
                            return false;
                        }

                        // ttl >= 0, try to acquire PubSubLock, if success, try to acquire TLock
                        try {
                            this.pubSubLock.tryLock(ttl, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            if (interruptibly) {
                                throw e;
                            } else {
                                // ignore InterruptedException, continue to try lock
                                this.pubSubLock.tryLock(ttl, TimeUnit.MILLISECONDS);
                            }
                        }
                    }
                }
            }
        } finally {
            // Cancel subscribe
            unSubscribe();
        }
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
                "if redis.call('SETNX', KEYS[1], '1') == 1 then " +
                        "    local expireTime = tonumber(ARGV[1]) " +
                        "    redis.call('PEXPIRE', KEYS[1], expireTime) " +
                        "    return nil" +
                        "else" +
                        "    return redis.call('PTTL', KEYS[1]) " +
                        "end"
                ;

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(unit.toMillis(leaseTime)));
        Object ttl = this.redisClientWrapper.executeLua(tryLockLua, getLockKey(), args);
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

    /**
     * @return pub/sub channel name
     */
    protected String getChannel() {
        return "TLock__channel_" + this.name;
    }

    /**
     * subscribe channel
     */
    protected Boolean subscribe(long waitTime, TimeUnit unit, boolean interruptibly) throws
            InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> future = this.redisClientWrapper.subscribe(getChannel());
        try {
            return future.get(waitTime, unit);
        } catch (InterruptedException e) {
            if (interruptibly) {
                throw e;
            }
            // ignore interruptException
            return future.get(waitTime, unit);
        }
    }

    /**
     * cancel subscribe
     */
    protected void unSubscribe(){
        this.redisClientWrapper.unSubscribe(getChannel());
    }
}
