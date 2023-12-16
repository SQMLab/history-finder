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
    @Column(name = "oracle_field_id", tag = true)
    Integer oracleFileId;
    @Column(name = "oracle_file_name", tag = true)
    String oracleFileName;
    @Column(name = "tracer_name", tag = true)
    String tracerName;
    @Column(name = "commit_hash")
    String commitHash;
    @Column(name = "committed_at")
    Instant committedAt;
    @Column(name = "diff")
    String diff;
    @Column(name = "diff_detail")
    String diffDetail;
    @Column(name = "created_at", timestamp = true)
    Instant createdAt;
}

