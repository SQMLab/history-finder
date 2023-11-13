package com.shahidul.git.log.oracle.core.mongo.entity;

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
public class TraceAnalysisEntity {
    Double precision;
    Double recall;
    List<CommitEntity> commits;
    /**
     * True Positive
     */
    List<CommitEntity> correctCommits;
    /**
     * False Positive
     */
    List<CommitEntity> incorrectCommits;
    /**
     * True Negative
     */
    List<CommitEntity> missingCommits;
}
