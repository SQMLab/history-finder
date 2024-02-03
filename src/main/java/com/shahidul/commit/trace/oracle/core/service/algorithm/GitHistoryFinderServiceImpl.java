package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.dto.Commit;
import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.dto.HistoryFinderOutput;
import rnd.git.history.finder.enums.LanguageType;
import rnd.git.history.finder.service.HistoryFinderServiceImpl;

/**
 * @author Shahidul Islam
 * @since 01/03/2024
 */
@Service
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



            traceEntity.getAnalysis().put(getTracerName(), AnalysisUdt.builder().commits(historyFinderOutput.getCommitList()
                    .stream().map(this::toCommitEntity).toList()).build());
            return traceEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    private CommitUdt toCommitEntity(Commit commitEntry) {
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder().tracerName(getTracerName());
        commitBuilder.parentCommitHash(null);
        commitBuilder.commitHash(commitEntry.getCommitHash());

        return commitBuilder
                .build();

    }
}
