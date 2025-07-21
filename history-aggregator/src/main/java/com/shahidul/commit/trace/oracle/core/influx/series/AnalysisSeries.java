package com.shahidul.commit.trace.oracle.core.influx.series;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
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
    @Column(name = "cc")
    Integer commitCount;
    @Column(name = "ccc")
    Integer correctCommitCount;
    @Column(name = "icc")
    Integer incorrectCommitCount;
    @Column(name = "mcc")
    Integer missingCommitCount;
    @Column(name = "translated_at", timestamp = true)
    Instant translatedAt;
}

