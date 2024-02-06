package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.config.AppProperty;
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

import java.util.ArrayList;
import java.util.List;

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
    public String parseChangeType(String rawChangeType) {
        //TODO : convert
        return rawChangeType;
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
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder()
                .tracerName(getTracerName())
                .commitHash(commitEntry.getCommitHash())
                .changeTags(null);
        if (parentEntry != null) {
            commitBuilder.parentCommitHash(parentEntry.getCommitHash())
                    .diff(Util.getDiff(parentEntry.getMethodCode(), commitEntry.getMethodCode()));
        }

        return commitBuilder
                .build();

    }
}
