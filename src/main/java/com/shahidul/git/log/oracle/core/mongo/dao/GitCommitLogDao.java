package com.shahidul.git.log.oracle.core.mongo.dao;

import com.shahidul.git.log.oracle.core.model.GitCommit;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.mongo.entity.GitCommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.GitLogEntity;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
public interface GitCommitLogDao {
    GitLogEntity toGitLogEntity(GitLog gitLog);
    GitCommitEntity gotCommitEntity(GitCommit commit);
}
