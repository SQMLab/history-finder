package com.shahidul.commit.trace.oracle.cmd.helper;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputCommit;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.model.OutputCommitDetail;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
import com.shahidul.commit.trace.oracle.util.ChangeTagUtil;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
                .endLine(commandLineInput.getEndLine())
                .commits(new ArrayList<>())
                .build();
    }

    @Override
    public TraceEntity loadOracle(InputOracle inputOracle, Integer optionalOracleFileId) {
        if (optionalOracleFileId != null) {
            return traceDao.findByOracleId(optionalOracleFileId);
        } else {
            String oracleHash = oracleHelperService.generateOracleHash(inputOracle);
            TraceEntity traceEntity = traceDao.findByOracleHash(oracleHash);
            if (traceEntity == null) {
                return oracleHelperService.build(inputOracle);
            } else {
                return traceEntity;
            }
        }
    }

    @Override
    public CommitTraceOutput readOutput(TraceEntity traceEntity, TracerName tracerName) {
        AnalysisUdt analysisUdt = traceEntity.getAnalysis().get(tracerName.getCode());
        List<InputCommit> commitList = analysisUdt.getCommits().stream().map(commitUdt -> InputCommit.builder()
                .commitHash(commitUdt.getCommitHash())
                .changeTags(commitUdt.getChangeTags())
                .build()).toList();
        List<OutputCommitDetail> commitDetailList = toCommitDetailList(analysisUdt.getCommits());
        return CommitTraceOutput.builder()
                .tracerName(tracerName.getCode())
                .displayTracerName(displayText(tracerName.name()))
                .repositoryName(traceEntity.getRepositoryName())
                .repositoryUrl(traceEntity.getRepositoryUrl())
                .startCommitHash(traceEntity.getStartCommitHash())
                .file(traceEntity.getFile())
                .fileName(Util.extractFileName(traceEntity.getFile()))
                .language(traceEntity.getLanguageType())
                .elementType(traceEntity.getElementType())
                .element(traceEntity.getElementName())
                .startLine(traceEntity.getStartLine())
                .endLine(traceEntity.getEndLine())
                .runtime(analysisUdt.getRuntime())
                .precision(analysisUdt.getPrecision())
                .recall(analysisUdt.getRecall())
                .commits(commitList)
                .commitMap(commitList.stream().collect(Collectors.toMap(InputCommit::getCommitHash,
                        commit-> ChangeTagUtil.toCodeShovelChangeText(commit.getChangeTags().stream().toList()),
                        (x,y)-> x, LinkedHashMap::new)))
                .commitHashes(analysisUdt.getCommits().stream().map(CommitUdt::getCommitHash).toList())
                .commitDetails(commitDetailList)
                .commitDetailMap(commitDetailList.stream().collect(Collectors.toMap(OutputCommitDetail::getCommitHash,
                                commit-> commit,
                                (x,y)-> x,
                                LinkedHashMap::new)))
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
                .newFile(commitUdt.getNewFile())
                .oldFile(commitUdt.getOldFile())
                .changeTags(commitUdt.getChangeTags())
                .displayChangeTags(displayChangeTags(commitUdt.getChangeTags()))
                .changeTagText(ChangeTagUtil.toCodeShovelChangeText(commitUdt.getChangeTags().stream().toList()))
                .author(commitUdt.getAuthor())
                .email(commitUdt.getEmail())
                .shortMessage(commitUdt.getShortMessage())
                .fullMessage(commitUdt.getFullMessage())
                .diff(commitUdt.getDiff())
                .commitUrl(commitUdt.getCommitUrl())
                .diffUrl(commitUdt.getDiffUrl())
                .authorSearchUrl(Util.getUserSearchUrl(commitUdt.getAuthor()))
                .oldFileUrl(commitUdt.getOldFilUrl())
                .newFileUrl(commitUdt.getNewFileUrl())
                .docDiff(commitUdt.getDocDiff())
                .diffDetail(commitUdt.getDiffDetail())
                .build();
    }
    private List<String> displayChangeTags(List<ChangeTag> changeTags) {
        if (changeTags == null){
            return Collections.emptyList();
        }
        return changeTags.stream()
                .map(tag-> displayText(tag.getCode()))
                .toList();

    }

    @NotNull
    private static String displayText(String tag) {
        return Arrays.stream(tag.split("_")).map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()).collect(Collectors.joining(" "));
    }
}
