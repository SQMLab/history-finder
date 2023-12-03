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
    @Column(tag = true)
    Integer oracleFileId;
    @Column(tag = true)
    String oracleFileName;
    @Column(tag = true)
    String tracerName;
    @Column
    Double precision;
    @Column
    Double recall;
    @Column
    Long runtime;
    @Column
    Integer commits;
    @Column
    Integer correctCommits;
    @Column
    Integer incorrectCommits;
    @Column
    Integer missingCommits;
    @Column(timestamp = true)
    Instant createdAt;
}

