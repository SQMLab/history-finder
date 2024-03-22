package com.shahidul.commit.trace.oracle.core.error.exception;

/**
 * @author Shahidul Islam
 * @since 3/22/2024
 */
public class BaseException extends RuntimeException{
    String code;
    String msg;

    public BaseException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public BaseException(String code, String msg, Throwable throwable) {
        super(throwable);
        this.code = code;
        this.msg = msg;
    }
}
