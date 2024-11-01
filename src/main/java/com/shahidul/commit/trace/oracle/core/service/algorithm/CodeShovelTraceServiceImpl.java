package com.shahidul.commit.trace.oracle.core.service.algorithm;

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
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author Shahidul Islam
 * @since 11/12/2023
 */
@Service("CODE_SHOVEL")
@AllArgsConstructor
public class CodeShovelTraceServiceImpl implements TraceService {
    AppProperty appProperty;

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
            String repositoryLocation = appProperty.getRepositoryBasePath() + "/" + traceEntity.getRepositoryName();

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
            traceEntity.getAnalysis().put(getTracerName(), AnalysisUdt.builder().commits(output.entrySet()
                    .stream().map(commitEntry -> toCommitEntity(commitEntry, traceEntity)).toList()).build());
            return traceEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private CommitUdt toCommitEntity(Map.Entry<String, Ychange> commitEntry, TraceEntity traceEntity) {
        Ychange change = commitEntry.getValue();
        com.google.gson.JsonObject json = change.toJsonObject();
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder().tracerName(getTracerName());
        JsonArray subChangeArray = null;
        String commitHash = commitEntry.getKey();
        String parentCommitHash = null;

        if (json.has("subchanges")){
            subChangeArray = json.get("subchanges").getAsJsonArray();
        }
        if (json.has("commitNameOld")) {
            parentCommitHash = json.get("commitNameOld").getAsString();
            commitBuilder.parentCommitHash(parentCommitHash);
        } else {
            parentCommitHash = findFirst(subChangeArray, "commitNameOld");
            commitBuilder.parentCommitHash(parentCommitHash);
        }
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
        //TODO : source & destination file if (json.has())

        if (json.has("diff")) {
            commitBuilder.diff(json.get("diff").getAsString());
        }else {
            commitBuilder.diff(findFirst(subChangeArray, "diff"));
        }
        Ycomparefunctionchange compareFunctionChange = change instanceof Ycomparefunctionchange? (Ycomparefunctionchange) change : null;
        if (compareFunctionChange == null && change instanceof Ymultichange){
            for (Ychange subChange : ((Ymultichange) change).getChanges()){
                if (subChange instanceof Ycomparefunctionchange){
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
                    .oldFilUrl(Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), parentCommitHash, oldFile, oldFunction.getNameLineNumber()))
                    .newFile(newFile)
                    .newFileUrl(Util.gitRawFileUrl(traceEntity.getRepositoryUrl(), commitHash, newFile, newFunction.getNameLineNumber()))
                    .fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                    .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0)
                    .newElement(newFunction.getBody());
        }

        return commitBuilder
                .build();

    }

    private LinkedHashSet<ChangeTag> toChangeTags(Ychange change) {
        LinkedHashSet<ChangeTag> changeTags = new LinkedHashSet<>();
        if (change instanceof Yintroduced) {
            changeTags.add(ChangeTag.INTRODUCTION);
        }
        if (change instanceof Ysignaturechange) {
            changeTags.add(ChangeTag.SIGNATURE);
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
            changeTags.add(ChangeTag.FILE_RENAME);
        }
        if (change instanceof  Ymultichange){
            for (Ychange subChange :  ((Ymultichange) change).getChanges()){
                changeTags.addAll(toChangeTags(subChange));
            }
        }
        return changeTags;
    }

    private String findFirst(JsonArray subChangeArray, String key){
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
