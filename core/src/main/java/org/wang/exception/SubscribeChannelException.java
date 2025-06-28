package org.wang.exception;

/**
 * @author wangjiabao
 */
public class SubscribeChannelException extends RuntimeException{

    public SubscribeChannelException() {
        super();
    }

    public SubscribeChannelException(String message) {
        super(message);
    }

    public SubscribeChannelException(Throwable cause) {
        super(cause);
    }

    public SubscribeChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
