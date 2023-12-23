package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.felixgrund.codeshovel.changes.*;
import com.felixgrund.codeshovel.entities.Yresult;
import com.felixgrund.codeshovel.execution.ShovelExecution;
import com.felixgrund.codeshovel.services.RepositoryService;
import com.felixgrund.codeshovel.services.impl.CachingRepositoryService;
import com.felixgrund.codeshovel.util.Utl;
import com.felixgrund.codeshovel.wrappers.Commit;
import com.felixgrund.codeshovel.wrappers.StartEnvironment;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Shahidul Islam
 * @since 11/12/2023
 */
@Service
@AllArgsConstructor
public class CodeShovelTraceServiceImpl implements TraceService {
    AppProperty appProperty;

    @Override
    public String getTracerName() {
        return TracerName.CODE_SHOVEL.getCode();
    }

    @Override
    public String parseChangeType(String rawChangeType) {
        //TODO : convert
        return rawChangeType;
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
                    .stream().map(this::toCommitEntity).toList()).build());
            return traceEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private CommitUdt toCommitEntity(Map.Entry<String, Ychange> commitEntry) {
        com.google.gson.JsonObject json = commitEntry.getValue().toJsonObject();
        CommitUdt.CommitUdtBuilder commitBuilder = CommitUdt.builder().tracerName(getTracerName());
        if (json.has("commitNameOld")){
            commitBuilder.parentCommitHash(json.get("commitNameOld").getAsString());
        }
        commitBuilder.commitHash(commitEntry.getKey());
        if (json.has("commitDate")){
            try {
                commitBuilder.committedAt(new SimpleDateFormat().parse(json.get("commitDate").getAsString()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (json.has("type")){
            commitBuilder.changeTags(toChangeTags(commitEntry.getValue()));
        }
        //TODO : source & destination file if (json.has())

        if (json.has("diff")){
            commitBuilder.diff(json.get("diff").getAsString());
        }
      /*  if (json.has("extendedDetails")){
            commitBuilder.diffDetail(json.get("extendedDetails").getAsString());
        }*/
     /*   if (json.has("commitsBetweenForRepo")){
            commitBuilder.commitsBetweenForRepo(json.get("commitsBetweenForRepo").getAsInt());
        }
        if (json.has("commitsBetweenForFile")){
            commitBuilder.commitsBetweenForFile(json.get("commitsBetweenForFile").getAsInt());
        }*/


 /*       if (json.has("commitDateOld")){
            commitBuilder.parentCommitTime(json.get("commitDateOld").getAsString());
        }
        if (json.has("commitAuthorOld")){
            commitBuilder.diffDetail(json.get("**************").getAsString());
        }
        if (json.has("subchanges")){
            commitBuilder.diffDetail(json.get("**************").getAsString());
        }*/

        return commitBuilder
                .build();

    }

    private Set<ChangeTag> toChangeTags(Ychange change){
        Set<ChangeTag> changeTags = new TreeSet<>();
        if (change instanceof Yintroduced){
            changeTags.add(ChangeTag.INTRODUCE);
        }
        if (change instanceof Ysignaturechange){
            changeTags.add(ChangeTag.SIGNATURE);
        }
        if (change instanceof Yrename){
            changeTags.add(ChangeTag.RENAME);
        }
        if (change instanceof Yreturntypechange){
            changeTags.add(ChangeTag.RETURN_TYPE);
        }
        if (change instanceof Yparameterchange){
            changeTags.add(ChangeTag.PARAMETER);
        }
        if (change instanceof Ymodifierchange){
            changeTags.add(ChangeTag.MODIFIER);
        }
        if (change instanceof Yexceptionschange){
            changeTags.add(ChangeTag.EXCEPTION);
        }

        if (change instanceof Ybodychange){
            changeTags.add(ChangeTag.BODY);
        }

        if (change instanceof Ymovefromfile){
            changeTags.add(ChangeTag.MOVE);
        }
        if (change instanceof Yfilerename){
            changeTags.add(ChangeTag.FILE_RENAME);
        }
        return changeTags;
    }
}
