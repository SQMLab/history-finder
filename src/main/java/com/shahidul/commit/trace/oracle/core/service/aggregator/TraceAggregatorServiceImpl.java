package com.shahidul.git.log.oracle.core.service.aggregator;

import com.shahidul.git.log.oracle.config.AppProperty;
import com.shahidul.git.log.oracle.core.enums.TrackerName;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.git.log.oracle.core.mongo.entity.AlgorithmExecutionUdt;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.git.log.oracle.core.mongo.repository.TraceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/13/2023
 */
@Service
@AllArgsConstructor
@Slf4j
public class TraceAggregatorServiceImpl implements TraceAggregatorService {
    TraceRepository traceRepository;
    AppProperty appProperty;

    @Override
    public void populateMetaData() {

        Map<String, RevCommit> commitMap = new HashMap<>();
        List<TraceEntity> traceEntityList = traceRepository.findAll().stream()
                .limit(100) //TODO : Remove limit
                .map(traceEntity -> {

                    Repository repository = null;
                    try {
                        repository = new GitServiceImpl().cloneIfNotExists(appProperty.getRepositoryBasePath() + "/" + traceEntity.getRepositoryName(), traceEntity.getRepositoryUrl());
                    } catch (Exception e) {
                        throw new RuntimeException(traceEntity.getRepositoryUrl(), e);
                    }

                    traceEntity.setExpectedCommits(injectMetaData(traceEntity.getExpectedCommits(), repository, commitMap));
                    ;
                    //injectMetaData(traceEntity.getAggregatedCommits(), repository, commitMap);

                    Repository finalRepository = repository;
                    traceEntity.getAnalysis()
                            .values()
                            .stream()
                            .map(analysis -> {
                                analysis.setCommits(injectMetaData(analysis.getCommits(), finalRepository, commitMap));
                                return analysis;
                            }).toList();
                    return traceEntity;
                }).toList();
        traceRepository.saveAll(traceEntityList);
    }

    @Override
    @Transactional
    public void aggregate() {
        List<TraceEntity> traceEntityList = traceRepository.findAll().stream()
                .map(traceEntity -> {
                    List<CommitUdt> aggregatedList = traceEntity.getAnalysis()
                            .values()
                            .stream()
                            .map(AlgorithmExecutionUdt::getCommits)
                            .flatMap(List::stream)
                            .collect(Collectors.toMap(CommitUdt::getCommitHash, Function.identity(), (o1, o2) -> {
                                TrackerName trackerX = TrackerName.fromCode(o1.getTracerName());
                                TrackerName trackerY = TrackerName.fromCode(o2.getTracerName());
                                if( TrackerName.AGGREGATION_PRIORITY.indexOf(trackerX) <  TrackerName.AGGREGATION_PRIORITY.indexOf(trackerY)){
                                    return o1;
                                }else {
                                    return o2;
                                }
                            }))
                            .values()
                            .stream()
                            .sorted(Comparator.comparing(CommitUdt::getCommittedAt, Comparator.nullsFirst(Comparator.reverseOrder())))
                            .toList();
                    traceEntity.setAggregatedCommits(aggregatedList);
                    return traceEntity;
                }).toList();
        traceRepository.saveAll(traceEntityList);
    }

    private List<CommitUdt> injectMetaData(List<CommitUdt> commitUdtList, Repository repository,  Map<String, RevCommit> cachedCommitMap){
        return Optional.of(commitUdtList)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(commitUdt -> {
                    String commitHash = commitUdt.getCommitHash();
                    if (!cachedCommitMap.containsKey(commitHash)){
                        RevCommit revCommit = null;
                        try {
                            revCommit = repository.parseCommit(ObjectId.fromString(commitHash));
                        } catch (IOException e) {
                            log.warn("Commit parse exception {}", e);
                        }
                        cachedCommitMap.put(commitHash, revCommit);
                    }
                    if (cachedCommitMap.containsKey(commitHash)){
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
                }).toList();
    }
}
