package com.shahidul.git.log.oracle.core.mongo.dao;

import com.shahidul.git.log.oracle.core.model.GitCommit;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.mongo.entity.GitCommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.GitLogEntity;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Repository
public class GitCommitLogDaoImpl implements GitCommitLogDao {
    @Override
    public GitLogEntity toGitLogEntity(GitLog gitLog) {
        return GitLogEntity.builder()
                .repositoryName(gitLog.getRepositoryName())
                .repositoryUrl(gitLog.getRepositoryWebURL())
                .startCommitId(gitLog.getStartCommitId())
                .filePath(gitLog.getFilePath())
                .functionName(gitLog.getFunctionName())
                .functionKey(gitLog.getFunctionKey())
                .startLine(gitLog.getFunctionStartLine())
                .build();
    }

    @Override
    public GitCommitEntity gotCommitEntity(GitCommit commit) {
        return GitCommitEntity.builder()
                .parentCommitId(commit.getParentCommitId())
                .commitId(commit.getCommitId())
                .commitTime(new Date(commit.getCommitTime()))
                .changeType(commit.getChangeType())
                .elementFileBefore(commit.getElementFileBefore())
                .elementFileAfter(commit.getElementFileAfter())
                .elementNameBefore(commit.getElementNameBefore())
                .elementNameAfter(commit.getElementNameAfter())
                .build();
    }
}
