package com.shahidul.commit.trace.oracle.core.service.aggregator;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
@Service
@AllArgsConstructor
@Slf4j
public class MetadataResolverServiceImpl implements MetadataResolverService {
    TraceDao traceDao;
    AppProperty appProperty;

    @Override
    public TraceEntity populateMetaData(TraceEntity traceEntity) {

        Map<String, RevCommit> commitMap = new HashMap<>();

        Repository repository = null;
        try {
            repository = new GitServiceImpl().cloneIfNotExists(appProperty.getRepositoryBasePath() + "/" + traceEntity.getRepositoryName(), traceEntity.getRepositoryUrl());
        } catch (Exception e) {
            throw new RuntimeException(traceEntity.getRepositoryUrl(), e);
        }

        traceEntity.setExpectedCommits(injectMetaData(traceEntity, traceEntity.getExpectedCommits(), repository, commitMap));
        //injectMetaData(traceEntity.getAggregatedCommits(), repository, commitMap);

        Repository finalRepository = repository;
        traceEntity.getAnalysis()
                .values()
                .stream()
                .map(analysis -> {
                    analysis.setCommits(injectMetaData(traceEntity, analysis.getCommits(), finalRepository, commitMap));
                    return analysis;
                }).toList();
        return traceDao.save(traceEntity);
    }


    private List<CommitUdt> injectMetaData(TraceEntity traceEntity, List<CommitUdt> commitUdtList, Repository repository, Map<String, RevCommit> cachedCommitMap) {
        return Optional.of(commitUdtList)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(commitUdt -> {
                    String commitHash = commitUdt.getCommitHash();
                    if (!cachedCommitMap.containsKey(commitHash)) {
                        RevCommit revCommit = null;
                        try {
                            revCommit = repository.parseCommit(ObjectId.fromString(commitHash));
                        } catch (IOException e) {
                            log.warn("Commit parse exception", e);
                        }
                        cachedCommitMap.put(commitHash, revCommit);
                    }
                    if (cachedCommitMap.containsKey(commitHash)) {
                        RevCommit revCommit = cachedCommitMap.get(commitHash);
                        PersonIdent authorIdent = revCommit.getAuthorIdent();
                        if (authorIdent != null) {
                            commitUdt.setAuthor(authorIdent.getName());
                            commitUdt.setEmail(authorIdent.getEmailAddress());
                        }
                        commitUdt.setShortMessage(revCommit.getShortMessage());
                        commitUdt.setFullMessage(revCommit.getFullMessage());
                        commitUdt.setCommittedAt(new Date(1000L * revCommit.getCommitTime()));
                    }
                    return commitUdt;
                })
                .map(commitUdt -> {
                    commitUdt.setDiffUrl(Util.getDiffUrl(traceEntity.getRepositoryUrl(), commitUdt.getParentCommitHash(), commitUdt.getCommitHash(), commitUdt.getNewFile()));
                    if (commitUdt.getNewFileUrl() == null) {
                        if (commitUdt.getOldFilUrl() != null) {
                            commitUdt.setOldFilUrl(Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), commitUdt.getParentCommitHash(), commitUdt.getOldFile(), commitUdt.getStartLine()));
                        }
                        commitUdt.setNewFileUrl(Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), commitUdt.getCommitHash(), commitUdt.getNewFile(), commitUdt.getStartLine()));
                    }

                    return commitUdt;
                })
                .toList();
    }
}
