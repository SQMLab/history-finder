package com.shahidul.commit.trace.oracle.core.influx.series;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

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
    @Column(name = "start_line")
    Integer startLine;
    @Column(name = "end_line")
    Integer endLine;
    @Column(name = "old_file")
    String oldFile;
    @Column(name = "new_file")
    String newFile;
    @Column(name = "file_renamed")
    Integer fileRenamed;
    @Column(name = "file_moved")
    Integer fileMoved;
    @Column(name = "old_element")
    String oldElement;
    @Column(name = "new_element")
    String newElement;
    @Column(name = "set_change_tag")
    Set<ChangeTag> changeTagSet;
    @Column(name = "diff_url")
    String diffUrl;
    @Column(name = "diff")
    String diff;
    @Column(name = "diff_detail")
    String diffDetail;
    @Column(name = "translated_at", timestamp = true)
    Instant translatedAt;
}

