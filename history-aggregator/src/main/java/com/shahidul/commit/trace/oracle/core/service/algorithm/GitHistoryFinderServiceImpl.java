package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.dto.ChangeTag;
import rnd.git.history.finder.dto.CommitTraceOutput;
import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.dto.OutputCommitDetail;
import rnd.git.history.finder.enums.LanguageType;
import rnd.git.history.finder.service.HistoryFinderServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shahidul Islam
 * @since 01/03/2024
 */
@Service("HISTORY_FINDER")
@AllArgsConstructor
@Slf4j
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
                    .cloneDirectory(TextUtils.isBlank(traceEntity.getCloneDirectory()) ? appProperty.getRepositoryBasePath() : traceEntity.getCloneDirectory())
                    .repositoryUrl(traceEntity.getRepositoryUrl())
                    .startCommitHash(traceEntity.getStartCommitHash())
                    .repositoryName(traceEntity.getRepositoryName())
                    .languageType(LanguageType.JAVA)
                    .file(traceEntity.getFile())
                    .methodName(traceEntity.getElementName())
                    .startLine(traceEntity.getStartLine())
                    .build();
            HistoryFinderServiceImpl historyFinderService = new HistoryFinderServiceImpl();
            CommitTraceOutput historyFinderOutput = historyFinderService.findSync(historyFinderInput);


            List<OutputCommitDetail> historyEntryList = historyFinderOutput.getCommitDetails();
            List<CommitUdt> commitUdtList = new ArrayList<>();

            for (int i = 0; i < historyEntryList.size(); i++) {
                commitUdtList.add(toCommitEntity(historyEntryList.get(i)));
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


    private CommitUdt toCommitEntity(OutputCommitDetail historyEntry) {
        List<ChangeTag> orderedTagList = historyEntry.getChangeTags()
                .stream()
                .map(tag -> parseChangeType(tag.getCode())).distinct().sorted(ChangeTag.NATURAL_ORDER).collect(Collectors.toList());

        String oldFile = historyEntry.getOldFile();
        String newFile = historyEntry.getNewFile();
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder()
                .tracerName(getTracerName())
                .commitHash(historyEntry.getCommitHash())
                .changeTags(orderedTagList)
                .codeFragment(historyEntry.getNewCode())
                .documentation(historyEntry.getNewDoc())
                .parentCommitHash(historyEntry.getParentCommitHash())
                .ancestorCommitHash(historyEntry.getAncestorCommitHash())
                .newFile(newFile)
                .newFileUrl(historyEntry.getNewFileUrl())
                .diff(historyEntry.getDiff())
                .docDiff(historyEntry.getDocDiff())
                .startLine(historyEntry.getStartLine())
                .endLine(historyEntry.getEndLine())
                .oldFile(oldFile)
                .oldFilUrl(historyEntry.getOldFileUrl());
        if (oldFile != null) {
            commitBuilder.fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                    .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0);
        }
        return commitBuilder.build();

    }
}
