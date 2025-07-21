package com.shahidul.commit.trace.oracle.api.payload;

import lombok.Builder;
import lombok.Data;

/**
 * @since 4/3/2024
 */
@Data
@Builder
public class ErrorResponse {
    String code;
    String msg;
}
