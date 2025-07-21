package rnd.method.history.commit.trace.oracle.core.error.exception;

import lombok.Getter;

/**
 * @since 3/22/2024
 */
@Getter
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
