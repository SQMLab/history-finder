package rnd.method.history.commit.trace.oracle.core.error.exception;

import rnd.method.history.commit.trace.oracle.core.error.CtoError;

/**
 * @since 3/22/2024
 */
public class CtoException extends BaseException{
    public CtoException(CtoError error) {
        super(error.getCode(), error.getMsg());
    }

    public CtoException(CtoError error, Throwable throwable) {
        super(error.getCode(), error.getMsg(), throwable);
    }
}
