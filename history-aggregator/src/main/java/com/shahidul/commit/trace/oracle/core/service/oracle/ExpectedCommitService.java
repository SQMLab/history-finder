package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import rnd.git.history.finder.dto.ChangeTag;

import java.util.List;

/**
 * @since 3/19/2024
 */
public interface ExpectedCommitService {
    CommitUdt findCommit(String oracleFileName, String commitHash, TracerName fromTracer);
    CommitUdt deleteCommit(String oracleFileName, String commitHash);
    CommitUdt addCommit(String oracleFileName, String commitHash, TracerName fromTracer);
    CommitUdt updateTags(String oracleFileName, String commitHash, TracerName fromTracer, List<ChangeTag> changeTagSet);
}
