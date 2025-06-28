package org.wang.lock;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author wangjiabao
 */
public class WatchDog {

    private static final Map<String, FutureTask<Boolean>> RENEWAL_LOCK_MAP = new ConcurrentHashMap<>();

    private ScheduledExecutorService executorService;

    private AbstractTLock abstractTLock;

    public WatchDog(AbstractTLock abstractTLock) {
        this.abstractTLock = abstractTLock;
        this.executorService = new ScheduledThreadPoolExecutor(2);
    }

    public void renewalLock(String lockName, long leaseTime, long intervalTime, TimeUnit unit) {
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            boolean renewal = this.abstractTLock.renewal(leaseTime, unit);
            if (!renewal) {
                throw new RuntimeException(lockName + " renewal lock failed.");
            }
            return true;
        });

        RENEWAL_LOCK_MAP.put(lockName, task);
        // schedule task
        this.executorService.schedule(task, intervalTime, unit);
    }
    
    public void cancelRenewalLock(String lockName) {
        RENEWAL_LOCK_MAP.get(lockName).cancel(true);
    }
}
