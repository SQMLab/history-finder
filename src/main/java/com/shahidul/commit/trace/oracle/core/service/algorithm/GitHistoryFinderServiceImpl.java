package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.dto.*;
import rnd.git.history.finder.enums.LanguageType;
import rnd.git.history.finder.service.HistoryFinderServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 01/03/2024
 */
@Service("HISTORY_FINDER")
@AllArgsConstructor
public class GitHistoryFinderServiceImpl implements TraceService {
    AppProperty appProperty;

    @Override
    public String getTracerName() {
        return TracerName.HISTORY_FINDER.getCode();
    }

    @Override
    public ChangeTag parseChangeType(String rawChangeType) {

        return ChangeTag.fromTag(rawChangeType);
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {
        try {

            HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                    .cacheDirectory(appProperty.getRepositoryBasePath())
                    .repositoryUrl(traceEntity.getRepositoryUrl())
                    .startCommitHash(traceEntity.getStartCommitHash())
                    .repositoryName(traceEntity.getRepositoryName())
                    .languageType(LanguageType.JAVA)
                    .file(traceEntity.getFile())
                    .methodName(traceEntity.getElementName())
                    .startLine(traceEntity.getStartLine())
                    .build();
            HistoryFinderServiceImpl historyFinderService = new HistoryFinderServiceImpl();
            HistoryFinderOutput historyFinderOutput = historyFinderService.findSync(historyFinderInput);


            List<HistoryEntry> historyEntryList = historyFinderOutput.getHistoryEntryList();
            List<CommitUdt> commitUdtList = new ArrayList<>();

            for (int i = 0; i < historyEntryList.size(); i++) {
                commitUdtList.add(toCommitEntity(historyEntryList.get(i), traceEntity));
            }


         /*
            List<Commit> commitList = historyFinderOutput.getCommitList();
           for (int i = 0; i < commitList.size(); i++) {
                commitUdtList.add(toCommitEntity(commitList.get(i), i + 1 < commitList.size() ? commitList.get(i + 1) : null));
            }*/
            traceEntity.setMethodId(historyFinderOutput.getMethodId());
            traceEntity.getAnalysis().put(getTracerName(), AnalysisUdt.builder()
                    .commits(commitUdtList)
                    .analyzedCommitCount(historyFinderOutput.getAnalyzedCommitCount())
                    .methodId(historyFinderOutput.getMethodId())
                    .build());
            return traceEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private CommitUdt toCommitEntity(Commit commitEntry, Commit parentEntry) {
        Set<ChangeTag> changeTags = commitEntry.getChangeTags()
                .stream()
                .map(tag -> parseChangeType(tag.getCode()))
                .collect(Collectors.toCollection(HashSet::new));
        String newFile = commitEntry.getMethodContainerFile();
        List<ChangeTag> orderedTagList = new ArrayList<>(changeTags);
        orderedTagList.sort(ChangeTag.NATURAL_ORDER);
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder()
                .tracerName(getTracerName())
                .commitHash(commitEntry.getCommitHash())
                .parentCommitHash(commitEntry.getParentCommitHash())
                .changeTags(orderedTagList)
                .codeFragment(commitEntry.getMethodCode())
                .documentation(commitEntry.getDocumentation())
                .newFile(newFile)
                .diff(Util.getDiff(parentEntry != null ? parentEntry.getMethodCode() : null, commitEntry.getMethodCode()))
                .docDiff(Util.getDiff(parentEntry != null ? parentEntry.getDocumentation() : null, commitEntry.getDocumentation()))
                .startLine(commitEntry.getStartLine())
                .endLine(commitEntry.getEndLine());
        if (parentEntry != null) {
            String oldFile = parentEntry.getMethodContainerFile();
            commitBuilder.oldFile(oldFile)
                    .fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                    .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0);
        }

        return commitBuilder
                .build();

    }

    private CommitUdt toCommitEntity(HistoryEntry historyEntry, TraceEntity traceEntity) {
        Set<ChangeTag> changeTags = historyEntry.getChangeTagSet()
                .stream()
                .map(tag -> parseChangeType(tag.getCode()))
                .collect(Collectors.toCollection(HashSet::new));
        MethodHolder newMethodHolder = historyEntry.getNewMethodHolder();
        String newFile = newMethodHolder.getFile();
        MethodHolder oldMethodHolder = historyEntry.getOldMethodHolder();
        String oldFile = oldMethodHolder != null ? oldMethodHolder.getFile() : null;

        int startLine = newMethodHolder.getMethodSourceInfo().getStartLine();
        List<ChangeTag> orderedTagList = new ArrayList<>(changeTags);
        orderedTagList.sort(ChangeTag.NATURAL_ORDER);
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder()
                .tracerName(getTracerName())
                .commitHash(newMethodHolder.getCommitHash())
                .changeTags(orderedTagList)
                .codeFragment(newMethodHolder.getMethodSourceInfo().getMethodRawSourceCode())
                .documentation(rnd.git.history.finder.Util.extractJavaDoc(newMethodHolder.getMethodSourceInfo().getMethodDeclaration()))
                .parentCommitHash(oldMethodHolder != null ? oldMethodHolder.getCommitHash() : null)
                .newFile(newFile)
                .newFileUrl(Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), newMethodHolder.getCommitHash(), newFile, startLine))
                .diff(Util.getDiff(oldMethodHolder != null ? oldMethodHolder.getMethodSourceInfo().getFullCode() : null, newMethodHolder.getMethodSourceInfo().getFullCode()))
                .docDiff(Util.getDiff(oldMethodHolder != null ? rnd.git.history.finder.Util.extractJavaDoc(oldMethodHolder.getMethodSourceInfo().getMethodDeclaration()) : null,
                        rnd.git.history.finder.Util.extractJavaDoc(newMethodHolder.getMethodSourceInfo().getMethodDeclaration())))
                .startLine(startLine)
                .endLine(newMethodHolder.getMethodSourceInfo().getEndLine())
                .oldFile(oldFile);
        if (oldFile != null) {
            commitBuilder.fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                    .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0)
                    .oldFilUrl(Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), oldMethodHolder.getCommitHash(), oldFile, oldMethodHolder.getMethodSourceInfo().getStartLine()));
        }
        return commitBuilder.build();

    }
}
