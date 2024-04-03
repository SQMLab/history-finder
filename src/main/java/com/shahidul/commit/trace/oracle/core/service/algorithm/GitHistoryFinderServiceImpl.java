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
import rnd.git.history.finder.dto.Commit;
import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.dto.HistoryFinderOutput;
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


            List<Commit> commitList = historyFinderOutput.getCommitList();
            List<CommitUdt> commitUdtList = new ArrayList<>();

            for (int i = 0; i < commitList.size(); i++) {
                commitUdtList.add(toCommitEntity(commitList.get(i), i + 1 < commitList.size() ? commitList.get(i + 1) : null));
            }
            traceEntity.getAnalysis().put(getTracerName(), AnalysisUdt.builder()
                    .commits(commitUdtList)
                    .build());
            return traceEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private CommitUdt toCommitEntity(Commit commitEntry, Commit parentEntry) {
        LinkedHashSet<ChangeTag> changeTags = commitEntry.getChangeTags()
                .stream()
                .map(tag -> parseChangeType(tag))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String newFile = commitEntry.getMethodContainerFile();
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder()
                .tracerName(getTracerName())
                .commitHash(commitEntry.getCommitHash())
                .changeTags(changeTags)
                .codeFragment(commitEntry.getMethodCode())
                .documentation(commitEntry.getDocumentation())
                .newFile(newFile);
        if (parentEntry != null) {
            String oldFile = parentEntry.getMethodContainerFile();
            commitBuilder.oldFile(oldFile)
                    .fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                    .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0)
                    .parentCommitHash(parentEntry.getCommitHash())
                    .diff(Util.getDiff(parentEntry.getMethodCode(), commitEntry.getMethodCode()))
                    .docDiff(Util.getDiff(parentEntry.getDocumentation(), commitEntry.getDocumentation()));
        }

        return commitBuilder
                .build();

    }
}
