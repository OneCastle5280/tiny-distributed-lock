package org.wang.constants;

import java.util.concurrent.TimeUnit;

/**
 * @author wangjiabao
 */
public interface TLcokConstants {

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

}
