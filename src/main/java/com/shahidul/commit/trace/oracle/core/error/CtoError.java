package com.shahidul.commit.trace.oracle.core.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Shahidul Islam
 * @since 3/22/2024
 */
@AllArgsConstructor
@Getter
public enum CtoError {
    Commit_Not_Found("1001", "Commit not found"),
    Trace_Not_Found("1002", "Trace not found"),
    File_Read_Error("1003", "File read error"),
    Oracle_File_Write_Error("1004", "Oracle file write error"),
    Commit_Already_Exist("1005", "Commit already exist"),
    Git_Checkout_Failed("1006", "Failed to checkout commit"),
    Java_Method_Parsing_Failed("1007", "Failed to parse methods"),
    Trace_Execution_Failed("1008", "Failed to execute trace"),
    ;
    private String code;
    private String msg;
}
