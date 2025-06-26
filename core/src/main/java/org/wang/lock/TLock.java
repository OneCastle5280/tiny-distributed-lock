package org.wang.lock;

import java.util.concurrent.locks.Lock;

/**
 * Tiny Distributed lock
 *
 * @author wangjiabao
 */
public interface TLock extends Lock {

    /**
     * TLock name
     *
     * @return
     */
    String getName();

}
