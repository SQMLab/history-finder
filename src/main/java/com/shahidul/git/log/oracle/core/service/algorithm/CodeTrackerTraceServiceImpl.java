package com.shahidul.git.log.oracle.core.service.algorithm;

import com.shahidul.git.log.oracle.config.AppProperty;
import com.shahidul.git.log.oracle.core.enums.TrackerName;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceAnalysisEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import org.codetracker.api.CodeTracker;
import org.codetracker.api.History;
import org.codetracker.api.MethodTracker;
import org.codetracker.element.Method;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Service
@AllArgsConstructor
public class CodeTrackerTraceServiceImpl implements TraceService {
    AppProperty appProperty;

    @Override
    public String getTracerName() {
        return TrackerName.CODE_TRACKER.getCode();
    }

    @Override
    public String parseChangeType(String rawChangeType) {
        return rawChangeType;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {
        GitService gitService = new GitServiceImpl();
        try (Repository repository = gitService.cloneIfNotExists(appProperty.getRepositoryBasePath() + "/" + traceEntity.getRepositoryName(),
                traceEntity.getRepositoryUrl())) {
            MethodTracker methodTracker = CodeTracker.methodTracker()
                    .repository(repository)
                    .filePath(traceEntity.getFilePath())
                    .startCommitId(traceEntity.getCommitHash())
                    .methodName(traceEntity.getElementName())
                    .methodDeclarationLineNumber(traceEntity.getStartLine())
                    .build();

            History<Method> methodHistory = methodTracker.track();


            List<CommitEntity> gitCommitList = methodHistory.getHistoryInfoList().stream()
                    .map(this::toCommitDiff)
                    .collect(Collectors.toList());
            traceEntity.getAnalysis().put(getTracerName(), TraceAnalysisEntity.builder().commits(gitCommitList).build());
            return traceEntity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private CommitEntity toCommitDiff(History.HistoryInfo<Method> historyInfo) {
        return CommitEntity.builder()
                .tracerName(getTracerName())
                .parentCommitHash(historyInfo.getParentCommitId())
                .commitHash(historyInfo.getCommitId())
                .committedAt(new Date(historyInfo.getCommitTime()))
                .changeType(parseChangeType(historyInfo.getChangeType().toString()))
                .elementFileBefore(historyInfo.getElementBefore().getFilePath())
                .elementFileAfter(historyInfo.getElementAfter().getFilePath())
                .elementNameBefore(historyInfo.getElementBefore().getName())
                .elementNameAfter(historyInfo.getElementAfter().getName())
                .build();
    }
}
