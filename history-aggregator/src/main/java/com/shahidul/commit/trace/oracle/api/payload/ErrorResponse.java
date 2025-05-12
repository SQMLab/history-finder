package com.shahidul.commit.trace.oracle.api.payload;

import lombok.Builder;
import lombok.Data;

/**
 * @author Shahidul Islam
 * @since 4/3/2024
 */
@Data
@Builder
public class ErrorResponse {
    String code;
    String msg;
}
