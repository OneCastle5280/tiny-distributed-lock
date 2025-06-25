package org.wang.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Tiny Distributed lock
 *
 * @author wangjiabao
 */
public interface TLock extends Lock {

    /**
     * default wait time, -1: waiting forever
     */
    Long DEFAULT_WAIT_TIME = -1L;

    /**
     * default lease time
     */
    Long DEFAULT_LEASE_TIME = 30L;

    /**
     * default TimeUnit
     */
    TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * TLock name
     *
     * @return
     */
    String getName();

}
