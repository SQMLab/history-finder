package com.shahidul.git.log.oracle.core.mongo.dao;

import com.shahidul.git.log.oracle.core.model.GitCommit;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
public interface GitCommitLogDao {
    TraceEntity toGitLogEntity(GitLog gitLog);
    CommitEntity gotCommitEntity(GitCommit commit);
}
