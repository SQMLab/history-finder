package rnd.method.history.commit.trace.oracle.core.model;

import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import lombok.Builder;
import lombok.Data;

/**
 * @since 2025-07-09
 */
@Builder
@Data
public class HistoryInputParam {
    String repositoryUrl;
    String startCommitHash;
    String file;
    String methodName;
    Integer startLine;
    Integer endLine;
    Integer oracleFileId;
    TracerName tracerName;
}
