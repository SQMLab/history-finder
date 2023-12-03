package com.shahidul.commit.trace.oracle.core.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
//AlgorithmExecutionEntity
public class AnalysisUdt {
    Double precision;
    Double recall;
    Long runtime;
    List<CommitUdt> commits;
    /**
     * True Positive
     */
    List<CommitUdt> correctCommits;
    /**
     * False Positive
     */
    List<CommitUdt> incorrectCommits;
    /**
     * True Negative
     */
    List<CommitUdt> missingCommits;
}
