package com.shahidul.commit.trace.oracle.core.service.aggregator;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.git.CtoGitService;
import com.shahidul.commit.trace.oracle.core.service.git.CtoGitServiceImpl;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
        CtoGitService ctoGitService = new CtoGitServiceImpl(repository);
        return Optional.of(commitUdtList)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(commitUdt -> {
                    String commitHash = commitUdt.getCommitHash();
                    cacheRevCommit(repository, cachedCommitMap, commitHash);
                    cacheRevCommit(repository, cachedCommitMap, commitUdt.getParentCommitHash());
                    String ancestorCommitHash = commitUdt.getAncestorCommitHash();
                    cacheRevCommit(repository, cachedCommitMap, ancestorCommitHash);
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
                        if (ancestorCommitHash != null && cachedCommitMap.containsKey(ancestorCommitHash)){
                            RevCommit revAncestorCommit = cachedCommitMap.get(ancestorCommitHash);
                            int commitTimeDiffInSecond = revCommit.getCommitTime() - revAncestorCommit.getCommitTime();
                            double daysBetweenCommits = (double) commitTimeDiffInSecond / (60 * 60 * 24);
                            commitUdt.setDaysBetweenCommits(new BigDecimal(daysBetweenCommits).setScale(2, RoundingMode.HALF_UP).doubleValue());
                            commitUdt.setCommitCountBetweenForRepo(ctoGitService.countCommit(cachedCommitMap.get(commitHash), cachedCommitMap.get(ancestorCommitHash), null));
                            commitUdt.setCommitCountBetweenForFile(ctoGitService.countCommit(cachedCommitMap.get(commitHash), cachedCommitMap.get(ancestorCommitHash), commitUdt.getNewFile()));
                        }

                    }
                    return commitUdt;
                })
                .map(commitUdt -> {
                    commitUdt.setCommitUrl(Util.getCommitUrl(traceEntity.getRepositoryUrl(), commitUdt.getCommitHash()));
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

    private static void cacheRevCommit(Repository repository, Map<String, RevCommit> cachedCommitMap, String commitHash) {
        if (!cachedCommitMap.containsKey(commitHash) && commitHash != null) {
            try {
                RevCommit revCommit = repository.parseCommit(ObjectId.fromString(commitHash));
                cachedCommitMap.put(commitHash, revCommit);
            } catch (IOException e) {
                log.warn("Commit parse exception", e);
            }
        }
    }
}
