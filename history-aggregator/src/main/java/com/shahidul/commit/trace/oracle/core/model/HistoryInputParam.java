package com.shahidul.commit.trace.oracle.core.model;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import lombok.Builder;
import lombok.Data;

/**
 * @author Shahidul Islam
 * @since 2025-07-09
 */
@Builder
@Data
public class HistoryInputParam {
    String repositoryHostName;
    String repositoryAccountName;
    String repositoryPath;
    String repositoryUrl;
    String repositoryName;
    String startCommitHash;
    String file;
    String methodName;
    Integer startLine;
    Integer endLine;
    Integer oracleFileId;
    TracerName tracerName;
}
