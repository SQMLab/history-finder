package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.felixgrund.codeshovel.services.RepositoryService;
import com.felixgrund.codeshovel.services.impl.CachingRepositoryService;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.util.Util;
import gr.uom.java.xmi.LocationInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.CodeTracker;
import org.codetracker.api.History;
import org.codetracker.api.MethodTracker;
import org.codetracker.change.Change;
import org.codetracker.element.Method;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Service("CODE_TRACKER")
@AllArgsConstructor
@Slf4j
public class CodeTrackerTraceServiceImpl implements TraceService {
    AppProperty appProperty;

    @Override
    public String getTracerName() {
        return TracerName.CODE_TRACKER.getCode();
    }

    @Override
    public ChangeTag parseChangeType(String rawChangeType) {
        return null;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {
        GitService gitService = new GitServiceImpl();
        String repositoryLocation = appProperty.getRepositoryBasePath() + "/" + traceEntity.getRepositoryName();

        try (Repository repository = gitService.cloneIfNotExists(repositoryLocation,
                traceEntity.getRepositoryUrl())) {
            RepositoryService cachingRepository = new CachingRepositoryService(new Git(repository), repository, traceEntity.getRepositoryName(), repositoryLocation);
            String startCommitId = traceEntity.getStartCommitHash();
            if ("HEAD".equalsIgnoreCase(traceEntity.getStartCommitHash())){
                startCommitId = repository.resolve(Constants.HEAD).getName();
            }
            MethodTracker methodTracker = CodeTracker.methodTracker()
                    .repository(repository)
                    .filePath(traceEntity.getFile())
                    .startCommitId(startCommitId)
                    .methodName(traceEntity.getElementName())
                    .methodDeclarationLineNumber(traceEntity.getStartLine())
                    .build();

            History<Method> methodHistory = methodTracker.track();


            List<CommitUdt> gitCommitList = methodHistory.getHistoryInfoList().reversed().stream()
                    .map(historyInfo -> toCommitDiff(cachingRepository, traceEntity, historyInfo))
                    .collect(Collectors.toList());
            traceEntity.getAnalysis().put(getTracerName(), AnalysisUdt.builder().commits(gitCommitList).build());
            return traceEntity;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    private CommitUdt toCommitDiff(RepositoryService cachingRepositoryService, TraceEntity traceEntity, History.HistoryInfo<Method> historyInfo) {

        Method newMethod = historyInfo.getElementAfter();
        LocationInfo newLocation = newMethod.getLocation();
        String newFile = newMethod.getFilePath();

        Method oldMethod = historyInfo.getElementBefore();
        String oldFile = oldMethod != null ? oldMethod.getFilePath() : null;
        String diff = null;
        String newCodeFragment = null;
        String oldCodeFragment = null;
        String parentCommitId = "0".equals(historyInfo.getParentCommitId()) ? null : historyInfo.getParentCommitId();
        try {
            String newFileContent = cachingRepositoryService.findFileContent(cachingRepositoryService.findCommitByName(historyInfo.getCommitId()), newFile);
            newCodeFragment = Util.readLineRange(newFileContent, newLocation.getStartLine(), newLocation.getEndLine());
            if (oldMethod != null && parentCommitId != null) {
                LocationInfo oldLocation = oldMethod.getLocation();
                String oldFileContent = cachingRepositoryService.findFileContent(cachingRepositoryService.findCommitByName(parentCommitId), oldFile);
                oldCodeFragment = Util.readLineRange(oldFileContent, oldLocation.getStartLine(), oldLocation.getEndLine());
            }
            diff = Util.getDiff(oldCodeFragment, newCodeFragment);
        } catch (Exception ex) {
            log.error("Diff error ", ex);
        }
        return CommitUdt.builder()
                .tracerName(getTracerName())
                .parentCommitHash(parentCommitId)
                .commitHash(historyInfo.getCommitId())
                .committedAt(new Date(historyInfo.getCommitTime()))
                .startLine(newLocation.getStartLine())
                .endLine(newLocation.getEndLine())
                .codeFragment(newCodeFragment)
                .changeTags(toChangeTagSet(historyInfo.getChangeList()))
                .oldFile(oldFile)
                .oldFilUrl(Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), parentCommitId, oldFile, historyInfo.getElementBefore().getLocation().getStartLine()))
                .newFile(newFile)
                .newFileUrl(Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), historyInfo.getCommitId(), newFile, newLocation.getStartLine()))
                .fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0)
                .oldElement(oldMethod.getName())
                .newElement(newMethod.getName())
                .diff(diff)
                .build();
    }

    private List<ChangeTag> toChangeTagSet(Set<Change> changeList) {
        Set<ChangeTag> changeTagSet = new HashSet<>();
        for (Change change:changeList) {
            Change.Type changeType = change.getType();
            if (changeType == Change.Type.INTRODUCED) {
                changeTagSet.add(ChangeTag.INTRODUCTION);
            }
            if (changeType == Change.Type.REMOVED) {
                throw new RuntimeException("Unknown change exception");
                //changeTagSet.add(ChangeTag.REMOVE);
            }
            if (changeType == Change.Type.CONTAINER_CHANGE) {
                changeTagSet.add(ChangeTag.FILE_MOVE);
            }
            if (changeType == Change.Type.BODY_CHANGE) {
                changeTagSet.add(ChangeTag.BODY);
            }
            if (changeType == Change.Type.RENAME) {
                changeTagSet.add(ChangeTag.RENAME);
            }
            if (changeType == Change.Type.MODIFIER_CHANGE || changeType == Change.Type.ACCESS_MODIFIER_CHANGE) {
                changeTagSet.add(ChangeTag.MODIFIER);
            }

            if (changeType == Change.Type.RETURN_TYPE_CHANGE) {
                changeTagSet.add(ChangeTag.RETURN_TYPE);
            }
            if (changeType == Change.Type.EXCEPTION_CHANGE) {
                changeTagSet.add(ChangeTag.EXCEPTION);
            }
            if (changeType == Change.Type.PARAMETER_CHANGE) {
                changeTagSet.add(ChangeTag.PARAMETER);
            }
            if (changeType == Change.Type.ANNOTATION_CHANGE) {
                changeTagSet.add(ChangeTag.ANNOTATION);
            }
            if (changeType == Change.Type.MOVED) {
                changeTagSet.add(ChangeTag.MOVE);
            }
            if (changeType == Change.Type.DOCUMENTATION_CHANGE) {
                changeTagSet.add(ChangeTag.DOCUMENTATION);
            }
            if (changeTagSet.isEmpty()) {
               log.warn("Change type mapping not found : {}", changeType);
            }
        }
        List<ChangeTag> orderedTagList = new ArrayList<>(changeTagSet);
        orderedTagList.sort(ChangeTag.NATURAL_ORDER);
        return orderedTagList;
    }
}
