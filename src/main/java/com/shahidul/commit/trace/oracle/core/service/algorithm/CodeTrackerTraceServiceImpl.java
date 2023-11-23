package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TrackerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AlgorithmExecutionUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
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


            List<CommitUdt> gitCommitList = methodHistory.getHistoryInfoList().stream()
                    .map(this::toCommitDiff)
                    .collect(Collectors.toList());
            traceEntity.getAnalysis().put(getTracerName(), AlgorithmExecutionUdt.builder().commits(gitCommitList).build());
            return traceEntity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private CommitUdt toCommitDiff(History.HistoryInfo<Method> historyInfo) {
        boolean isNewFile = historyInfo.getElementBefore().getFilePath().equals(historyInfo.getElementAfter().getFilePath());
        boolean isNewElement = historyInfo.getElementBefore().getName().equals(historyInfo.getElementAfter().getName());

        return CommitUdt.builder()
                .tracerName(getTracerName())
                .parentCommitHash(historyInfo.getParentCommitId())
                .commitHash(historyInfo.getCommitId())
                .committedAt(new Date(historyInfo.getCommitTime()))
                .changeType(parseChangeType(historyInfo.getChangeType().toString()))
                .renamedFile(isNewFile ? historyInfo.getElementAfter().getFilePath() : null)
                .renamedElement(isNewElement ? historyInfo.getElementAfter().getName() : null)
                .build();
    }
}
