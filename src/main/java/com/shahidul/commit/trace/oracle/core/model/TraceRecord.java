package com.shahidul.commit.trace.oracle.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Shahidul Islam
 * @since 3/23/2024
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TraceRecord {
    Map<String, InputTrace> traceMap = new HashMap<>();
}
