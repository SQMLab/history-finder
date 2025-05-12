package com.shahidul.commit.trace.oracle.api.controller;

import com.shahidul.commit.trace.oracle.api.payload.ErrorResponse;
import com.shahidul.commit.trace.oracle.core.error.exception.CtoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author Shahidul Islam
 * @since 4/3/2024
 */
@ControllerAdvice
public class CommonExceptionHandler{
    @ExceptionHandler(value = {CtoException.class})
    public ResponseEntity<ErrorResponse> handleCtoException(CtoException ctoException){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ctoException.getCode())
                .msg(ctoException.getMsg())
                .build();
        return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
