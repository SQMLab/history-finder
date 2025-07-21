package rnd.method.history.commit.trace.oracle.api.controller;

import rnd.method.history.commit.trace.oracle.api.payload.ErrorResponse;
import rnd.method.history.commit.trace.oracle.core.error.exception.BaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @since 4/3/2024
 */
@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(value = {BaseException.class})
    public ResponseEntity<ErrorResponse> handleCtoException(BaseException baseException) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(baseException.getCode())
                .msg(baseException.getMsg())
                .build();
        return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
