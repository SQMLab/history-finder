package com.shahidul.git.log.oracle.core.mongo.dao;

import com.shahidul.git.log.oracle.core.model.GitCommit;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Repository
public class GitCommitLogDaoImpl implements GitCommitLogDao {
    @Override
    public TraceEntity toGitLogEntity(GitLog gitLog) {
        return TraceEntity.builder()
                .repositoryName(gitLog.getRepositoryName())
                .repositoryUrl(gitLog.getRepositoryWebURL())
                .commitHash(gitLog.getStartCommitId())
                .filePath(gitLog.getFilePath())
                .functionName(gitLog.getFunctionName())
                .functionKey(gitLog.getFunctionKey())
                .startLine(gitLog.getFunctionStartLine())
                .build();
    }

    @Override
    public CommitEntity gotCommitEntity(GitCommit commit) {
        return CommitEntity.builder()
                .parentCommitHash(commit.getParentCommitId())
                .commitHash(commit.getCommitId())
                .commitTime(new Date(commit.getCommitTime()))
                .changeType(commit.getChangeType())
                .elementFileBefore(commit.getElementFileBefore())
                .elementFileAfter(commit.getElementFileAfter())
                .elementNameBefore(commit.getElementNameBefore())
                .elementNameAfter(commit.getElementNameAfter())
                .build();
    }
}
