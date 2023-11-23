package com.shahidul.commit.trace.oracle.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/16/2023
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AlgorithmExecution {
    List<Commit> commits;
}
