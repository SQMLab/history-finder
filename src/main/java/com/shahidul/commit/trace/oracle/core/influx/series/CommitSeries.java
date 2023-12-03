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
@Measurement(name = "commit")
@Data
public class CommitSeries {
    @Column(tag = true)
    Integer oracleFileId;
    @Column(tag = true)
    String oracleFileName;
    @Column(tag = true)
    String tracerName;
    @Column
    String commitHash;
    @Column(timestamp = true)
    Instant committedAt;
}

