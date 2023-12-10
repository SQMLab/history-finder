package com.shahidul.commit.trace.oracle.core.influx.series;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @author Shahidul Islam
 * @since 12/1/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Measurement(name = "analysis")
@Data
public class AnalysisSeries {
    @Column(name = "oracle_file_id", tag = true)
    Integer oracleFileId;
    @Column(name = "oracle_file_name", tag = true)
    String oracleFileName;
    @Column(name = "tracer_name", tag = true)
    String tracerName;
    @Column(name = "precision")
    Double precision;
    @Column(name = "recall")
    Double recall;
    @Column(name = "runtime")
    Long runtime;
    @Column(name = "commit_count")
    Integer commitCount;
    @Column(name = "correct_commit_count")
    Integer correctCommitCount;
    @Column(name = "incorrect_commit_count")
    Integer incorrectCommitCount;
    @Column(name = "missing_commit_count")
    Integer missingCommitCount;
    @Column(name = "created_at", timestamp = true)
    Instant createdAt;
}

