package rnd.method.history.commit.trace.oracle.core.service.algorithm;

import com.felixgrund.codeshovel.changes.*;
import com.felixgrund.codeshovel.entities.Yresult;
import com.felixgrund.codeshovel.execution.ShovelExecution;
import com.felixgrund.codeshovel.parser.Yfunction;
import com.felixgrund.codeshovel.services.RepositoryService;
import com.felixgrund.codeshovel.services.impl.CachingRepositoryService;
import com.felixgrund.codeshovel.util.Utl;
import com.felixgrund.codeshovel.wrappers.Commit;
import com.felixgrund.codeshovel.wrappers.StartEnvironment;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import rnd.method.history.commit.trace.oracle.config.AppProperty;
import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import rnd.method.history.commit.trace.oracle.core.error.CtoError;
import rnd.method.history.commit.trace.oracle.core.error.exception.CtoException;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.AdditionalCommitInfoUdt;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.CommitUdt;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;
import rnd.method.history.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.dto.ChangeTag;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @since 11/12/2023
 */
@Service("CODE_SHOVEL")
@AllArgsConstructor
@Slf4j
public class CodeShovelTraceServiceImpl implements TraceService {
    AppProperty appProperty;
    private static final Map<String, ChangeTag> CHANGE_TAG_MAP = new HashMap<>();

    @Override
    public String getTracerName() {
        return TracerName.CODE_SHOVEL.getCode();
    }

    @Override
    public ChangeTag parseChangeType(String rawChangeType) {
        //TODO : convert
        return null;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {
        try {
            String repositoryLocation = Util.getLocalProjectDirectory(traceEntity.getCloneDirectory(), appProperty.getRepositoryBasePath(), traceEntity.getRepositoryName());

            Repository repository = new GitServiceImpl().cloneIfNotExists(repositoryLocation, traceEntity.getRepositoryUrl());
            Git git = new Git(repository);
            RepositoryService repositoryService = new CachingRepositoryService(git, repository, traceEntity.getRepositoryName(), repositoryLocation);
            Commit startCommit = repositoryService.findCommitByName(traceEntity.getStartCommitHash());

            StartEnvironment startEnv = new StartEnvironment(repositoryService);
            startEnv.setRepositoryPath(repositoryLocation);
            startEnv.setFilePath(traceEntity.getFile());
            startEnv.setFunctionName(traceEntity.getElementName());
            startEnv.setFunctionStartLine(traceEntity.getStartLine());
            startEnv.setStartCommitName(traceEntity.getStartCommitHash());
            startEnv.setStartCommit(startCommit);
            startEnv.setFileName(Utl.getFileName(startEnv.getFilePath()));
            //startEnv.setOutputFilePath(outputFilePath);
            Yresult output = ShovelExecution.runSingle(startEnv, startEnv.getFilePath(), true);
            /*Throwing exception from here will cause to no null history
            if (output.isEmpty()){
                throw new CtoException(CtoError.CodeShovel_Failure);
            }*/
            traceEntity.getAnalysis().put(getTracerName(),
                    AnalysisUdt.builder()
                            .commits(output.entrySet().stream().map(commitEntry -> toCommitEntity(commitEntry, traceEntity)).toList())
                            .analyzedCommitCount(output.getNumCommitsSeen())
                            .methodId(output.getFunctionId())
                            .build());
            return traceEntity;
        } catch (Exception e) {
            throw new CtoException(CtoError.CodeShovel_Failure, e);
        }

    }

    private CommitUdt toCommitEntity(Map.Entry<String, Ychange> commitEntry, TraceEntity traceEntity) {
        Ychange change = commitEntry.getValue();
        com.google.gson.JsonObject json = change.toJsonObject();
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder().tracerName(getTracerName());
        String commitHash = commitEntry.getKey();
        String parentCommitHash = null;


        commitBuilder.commitHash(commitHash);
        if (json.has("commitDate")) {
            try {
                commitBuilder.committedAt(new SimpleDateFormat().parse(json.get("commitDate").getAsString()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        if (json.has("type")) {
            commitBuilder.changeTags(toChangeTags(change));
        }
        if (json.has("extendedDetails")) {
            commitBuilder.additionalInfo(parseExtendedDetail(json.get("extendedDetails").getAsJsonObject()));
        }
        JsonObject jsonDetail = null;
        JsonArray subChangeArray = null;

        if (json.has("subchanges")) {
            subChangeArray = json.get("subchanges").getAsJsonArray();
            if (subChangeArray.size() > 0) {
                jsonDetail = subChangeArray.get(0).getAsJsonObject();
            }
            commitBuilder.subChangeList(parseSubChanges(subChangeArray));
        }
        if (jsonDetail == null) {
            jsonDetail = json;
        }
        if (jsonDetail.has("commitNameOld")) {
            parentCommitHash = jsonDetail.get("commitNameOld").getAsString();
            commitBuilder.parentCommitHash(parentCommitHash);
        }

        if (jsonDetail.has("commitAuthor")) {
            commitBuilder.author(jsonDetail.get("commitAuthor").getAsString());
        }
        if (jsonDetail.has("commitAuthorOld")) {
            commitBuilder.oldAuthor(jsonDetail.get("commitAuthorOld").getAsString());
        }

        if (jsonDetail.has("diff")) {
            commitBuilder.diff(jsonDetail.get("diff").getAsString());
        } else {
            commitBuilder.diff(findFirst(subChangeArray, "diff"));
        }
        if (jsonDetail.has("actualSource")) {
            commitBuilder.codeFragment(jsonDetail.get("actualSource").getAsString());
        }
        Ycomparefunctionchange compareFunctionChange = change instanceof Ycomparefunctionchange ? (Ycomparefunctionchange) change : null;
        if (compareFunctionChange == null && change instanceof Ymultichange) {
            for (Ychange subChange : ((Ymultichange) change).getChanges()) {
                if (subChange instanceof Ycomparefunctionchange) {
                    compareFunctionChange = (Ycomparefunctionchange) subChange;
                    break;
                }
            }
        }
        if (compareFunctionChange != null) {
            Yfunction oldFunction = compareFunctionChange.getOldFunction();
            Yfunction newFunction = compareFunctionChange.getNewFunction();
            String oldFile = oldFunction.getSourceFilePath();
            String newFile = newFunction.getSourceFilePath();
            commitBuilder.startLine(newFunction.getNameLineNumber())
                    .endLine(newFunction.getEndLineNumber())
                    .oldFile(oldFile)
                    .oldFilUrl(rnd.git.history.finder.Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), parentCommitHash, oldFile, oldFunction.getNameLineNumber()))
                    .newFile(newFile)
                    .newFileUrl(rnd.git.history.finder.Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), commitHash, newFile, newFunction.getNameLineNumber()))
                    .fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                    .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0);
        }
        if (jsonDetail.has("path")) {
            commitBuilder.newFile(jsonDetail.get("path").getAsString());
        }

        return commitBuilder
                .build();

    }

    private AdditionalCommitInfoUdt parseExtendedDetail(JsonObject extendedInfo) {
        return AdditionalCommitInfoUdt.builder()
                .oldSignature(extendedInfo.has("oldValue") ? extendedInfo.get("oldValue").getAsString() : null)
                .newSignature(extendedInfo.has("newValue") ? extendedInfo.get("newValue").getAsString() : null)
                .oldFile(extendedInfo.has("oldPath") ? extendedInfo.get("oldPath").getAsString() : null)
                .newFile(extendedInfo.has("newPath") ? extendedInfo.get("newPath").getAsString() : null)
                .oldMethodName(extendedInfo.has("oldMethodName") ? extendedInfo.get("oldMethodName").getAsString() : null)
                .newMethodName(extendedInfo.has("newMethodName") ? extendedInfo.get("newMethodName").getAsString() : null)
                .build();

    }

    private List<AdditionalCommitInfoUdt> parseSubChanges(JsonArray subChangeArray) {
        List<AdditionalCommitInfoUdt> subChangeList = new ArrayList<>();
        for (int i = 0; i < subChangeArray.size(); i++) {
            JsonObject subChange = subChangeArray.get(i).getAsJsonObject();
            AdditionalCommitInfoUdt additionalInfo;
            if (subChange.has("extendedDetails")) {
                additionalInfo = parseExtendedDetail(subChange.get("extendedDetails").getAsJsonObject());
            } else {
                additionalInfo = AdditionalCommitInfoUdt.builder().build();
            }
            try {
                String changeTypeText = subChange.get("type").getAsString();
                additionalInfo.setChangeTag(CHANGE_TAG_MAP.get(changeTypeText));
            } catch (Exception e) {
                log.warn("Failed to detect change type", e);
            }
            subChangeList.add(additionalInfo);
        }
        return subChangeList;
    }

    private List<ChangeTag> toChangeTags(Ychange change) {
        Set<ChangeTag> changeTags = new HashSet<>();
        if (change instanceof Yintroduced) {
            changeTags.add(ChangeTag.INTRODUCTION);
        }
        if (change instanceof Yrename) {
            changeTags.add(ChangeTag.RENAME);
        }
        if (change instanceof Yreturntypechange) {
            changeTags.add(ChangeTag.RETURN_TYPE);
        }
        if (change instanceof Yparameterchange) {
            changeTags.add(ChangeTag.PARAMETER);
        }
        if (change instanceof Ymodifierchange) {
            changeTags.add(ChangeTag.MODIFIER);
        }
        if (change instanceof Yexceptionschange) {
            changeTags.add(ChangeTag.EXCEPTION);
        }

        if (change instanceof Ybodychange) {
            changeTags.add(ChangeTag.BODY);
        }

        if (change instanceof Ymovefromfile) {
            changeTags.add(ChangeTag.MOVE);
        }
        if (change instanceof Yfilerename) {
            changeTags.add(ChangeTag.FILE_MOVE);
        }
        if (change instanceof Ymultichange) {
            for (Ychange subChange : ((Ymultichange) change).getChanges()) {
                changeTags.addAll(toChangeTags(subChange));
            }
        }
        if (changeTags.isEmpty()) {
            throw new RuntimeException("Change tag mapping not found : " + change.getTypeAsString());
        }
        List<ChangeTag> changeList = new ArrayList<>(changeTags);
        changeList.sort(ChangeTag.NATURAL_ORDER);
        return changeList;
    }

    static {
        CHANGE_TAG_MAP.put("Yintroduced", ChangeTag.INTRODUCTION);
        CHANGE_TAG_MAP.put("Yrename", ChangeTag.RENAME);
        CHANGE_TAG_MAP.put("Yreturntypechange", ChangeTag.RETURN_TYPE);
        CHANGE_TAG_MAP.put("Yparameterchange", ChangeTag.PARAMETER);
        CHANGE_TAG_MAP.put("Ymodifierchange", ChangeTag.MODIFIER);
        CHANGE_TAG_MAP.put("Yexceptionschange", ChangeTag.EXCEPTION);
        CHANGE_TAG_MAP.put("Ybodychange", ChangeTag.BODY);
        CHANGE_TAG_MAP.put("Ymovefromfile", ChangeTag.MOVE);
        CHANGE_TAG_MAP.put("Yfilerename", ChangeTag.FILE_MOVE);
    }

    private String findFirst(JsonArray subChangeArray, String key) {
        if (subChangeArray != null) {
            for (int i = 0; i < subChangeArray.size(); i++) {
                if (subChangeArray.get(i).getAsJsonObject().has(key)) {
                    return subChangeArray.get(i).getAsJsonObject().get(key).getAsString();
                }
            }
        }
        return null;
    }
}
