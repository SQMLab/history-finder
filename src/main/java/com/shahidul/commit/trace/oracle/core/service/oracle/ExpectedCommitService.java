package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;

/**
 * @author Shahidul Islam
 * @since 3/19/2024
 */
public interface ExpectedCommitService {
    CommitUdt findCommit(String oracleFileName, String commitHash);
    CommitUdt deleteCommit(String oracleFileName, String commitHash);
    CommitUdt addCommit(String oracleFileName, String commitHash, TracerName fromTracer);
}
