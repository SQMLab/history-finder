package com.shahidul.git.log.oracle.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.git.log.oracle.core.model.GitCommit;
import com.shahidul.git.log.oracle.core.model.GitLog;
import com.shahidul.git.log.oracle.core.model.LogTracerInput;
import com.shahidul.git.log.oracle.core.model.LogTracerOutput;
import com.shahidul.git.log.oracle.core.mongo.entity.GitCommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.GitCommitTraceEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.GitLogEntity;
import org.codetracker.api.CodeTracker;
import org.codetracker.api.History;
import org.codetracker.api.MethodTracker;
import org.codetracker.element.Method;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Service
public class CodeTrackerGitTracerImpl implements GitTracer {

    @Override
    public LogTracerOutput trace(LogTracerInput tracerInput) {
        try {

            List<GitLogEntity> gitLogList = tracerInput.getGitLogEntityList()
                    .stream()
                    .limit(10)
                    .map(gitLogEntity -> {
                        try {

                            GitService gitService = new GitServiceImpl();
                            try (Repository repository = gitService.cloneIfNotExists("/dev/project/tmp/" + gitLogEntity.getRepositoryName(),
                                    gitLogEntity.getRepositoryUrl())) {
                                MethodTracker methodTracker = CodeTracker.methodTracker()
                                        .repository(repository)
                                        .filePath(gitLogEntity.getFilePath())
                                        .startCommitId(gitLogEntity.getStartCommitId())
                                        .methodName(gitLogEntity.getFunctionName())
                                        .methodDeclarationLineNumber(gitLogEntity.getStartLine())
                                        .build();

                                History<Method> methodHistory = methodTracker.track();


                                List<GitCommitEntity> gitCommitList = methodHistory.getHistoryInfoList().stream()
                                        .map(this::toCommitDiff)
                                        .collect(Collectors.toList());
                                 gitLogEntity.getOutput().put("code_tracker", GitCommitTraceEntity.builder().commitList(gitCommitList).build());
                                 return gitLogEntity;
                            }

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());
            return LogTracerOutput.builder()
                    .gitLogList(gitLogList).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GitCommitEntity toCommitDiff(History.HistoryInfo<Method> historyInfo) {
        return GitCommitEntity.builder()
                .parentCommitId(historyInfo.getParentCommitId())
                .commitId(historyInfo.getCommitId())
                .commitTime(new Date(historyInfo.getCommitTime()))
                .changeType(historyInfo.getChangeType().toString())
                .elementFileBefore(historyInfo.getElementBefore().getFilePath())
                .elementFileAfter(historyInfo.getElementAfter().getFilePath())
                .elementNameBefore(historyInfo.getElementBefore().getName())
                .elementNameAfter(historyInfo.getElementAfter().getName())
                .build();
    }
}
