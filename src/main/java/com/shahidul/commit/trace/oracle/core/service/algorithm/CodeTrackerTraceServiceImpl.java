package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import gr.uom.java.xmi.LocationInfo;
import lombok.AllArgsConstructor;
import org.codetracker.api.CodeTracker;
import org.codetracker.api.History;
import org.codetracker.api.MethodTracker;
import org.codetracker.change.Change;
import org.codetracker.element.Method;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
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
        return TracerName.CODE_TRACKER.getCode();
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
                    .startCommitId(traceEntity.getStartCommitHash())
                    .methodName(traceEntity.getElementName())
                    .methodDeclarationLineNumber(traceEntity.getStartLine())
                    .build();

            History<Method> methodHistory = methodTracker.track();


            List<CommitUdt> gitCommitList = methodHistory.getHistoryInfoList().stream()
                    .map(this::toCommitDiff)
                    .collect(Collectors.toList());
            traceEntity.getAnalysis().put(getTracerName(), AnalysisUdt.builder().commits(gitCommitList).build());
            return traceEntity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private CommitUdt toCommitDiff(History.HistoryInfo<Method> historyInfo) {
        boolean isNewElement = historyInfo.getElementBefore().getName().equals(historyInfo.getElementAfter().getName());

        LocationInfo newLocation = historyInfo.getElementAfter().getLocation();
        return CommitUdt.builder()
                .tracerName(getTracerName())
                .parentCommitHash(historyInfo.getParentCommitId())
                .commitHash(historyInfo.getCommitId())
                .committedAt(new Date(historyInfo.getCommitTime()))
                .startLine(newLocation.getStartLine())
                .endLine(newLocation.getEndLine())
                .codeFragment(null)
                .changeType(parseChangeType(historyInfo.getChangeType().toString()))
                .changeList(historyInfo.getChangeList().stream().map(Objects::toString).toList())
                .filePath(historyInfo.getElementAfter().getFilePath())
                .renamedElement(isNewElement ? historyInfo.getElementAfter().getName() : null)
                .build();
    }
}
