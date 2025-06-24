package org.wang.lock;

import java.util.concurrent.locks.Lock;

/**
 * Tiny Distributed lock
 *
 * @author wangjiabao
 */
public interface TDLock extends Lock {

    /**
     * acquire TDLock name
     *
     * @return TDLock name
     */
    String getName();
}
