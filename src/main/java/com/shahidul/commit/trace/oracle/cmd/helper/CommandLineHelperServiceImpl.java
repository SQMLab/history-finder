package com.shahidul.commit.trace.oracle.cmd.helper;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.model.OutputCommitDetail;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
@Component
@AllArgsConstructor
public class CommandLineHelperServiceImpl implements CommandLineHelperService {
    OracleHelperService oracleHelperService;
    TraceDao traceDao;

    @Override
    public InputOracle toInputOracle(CommandLineInput commandLineInput) {
        return InputOracle.builder()
                .repositoryUrl(commandLineInput.getRepositoryUrl())
                .repositoryName(commandLineInput.getRepositoryName())
                .startCommitHash(commandLineInput.getStartCommitHash())
                .file(commandLineInput.getFile())
                .language(commandLineInput.getLanguageType().name())
                .elementType("method")
                .element(commandLineInput.getMethodName())
                .startLine(commandLineInput.getStartLine())
                .endLine(null)
                .commits(new ArrayList<>())
                .build();
    }

    @Override
    public TraceEntity loadOracle(InputOracle inputOracle) {
        String oracleHash = oracleHelperService.generateOracleHash(inputOracle);
        TraceEntity traceEntity = traceDao.findByOracleHash(oracleHash);
        if (traceEntity == null) {
            return oracleHelperService.build(inputOracle);
        } else {
            return traceEntity;
        }
    }

    @Override
    public CommitTraceOutput readOutput(TraceEntity traceEntity, TracerName tracerName) {
        AnalysisUdt analysisUdt = traceEntity.getAnalysis().get(tracerName.getCode());
        return CommitTraceOutput.builder()
                .repositoryName(traceEntity.getRepositoryName())
                .repositoryUrl(traceEntity.getRepositoryUrl())
                .startCommitHash(traceEntity.getStartCommitHash())
                .file(traceEntity.getFile())
                .language(traceEntity.getLanguageType())
                .elementType(traceEntity.getElementType())
                .element(traceEntity.getElementName())
                .startLine(traceEntity.getStartLine())
                .endLine(traceEntity.getEndLine())
                .runtime(analysisUdt.getRuntime())
                .precision(analysisUdt.getPrecision())
                .recall(analysisUdt.getRecall())
                .commits(analysisUdt.getCommits().stream().map(CommitUdt::getCommitHash).toList())
                .commitDetails(toCommitDetailList(analysisUdt.getCommits()))
                .build();
    }

    private List<OutputCommitDetail> toCommitDetailList(List<CommitUdt> commitUdtList) {
        return commitUdtList.stream()
                .map(this::toOutputCommitDetail)
                .toList();
    }

    private OutputCommitDetail toOutputCommitDetail(CommitUdt commitUdt) {
        return OutputCommitDetail.builder()
                .commitHash(commitUdt.getCommitHash())
                .committedAt(commitUdt.getCommittedAt())
                .startLine(commitUdt.getStartLine())
                .endLine(commitUdt.getEndLine())
                .file(commitUdt.getNewFile())
                .changeTags(commitUdt.getChangeTags())
                .email(commitUdt.getEmail())
                .shortMessage(commitUdt.getShortMessage())
                .fullMessage(commitUdt.getFullMessage())
                .diff(commitUdt.getDiff())
                .diff(commitUdt.getDiff())
                .docDiff(commitUdt.getDocDiff())
                .diffDetail(commitUdt.getDiffDetail())
                .build();
    }
}
