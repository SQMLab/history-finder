package com.shahidul.git.log.oracle.core.service;

import com.felixgrund.codeshovel.changes.Ychange;
import com.felixgrund.codeshovel.entities.Yresult;
import com.felixgrund.codeshovel.execution.ShovelExecution;
import com.felixgrund.codeshovel.services.RepositoryService;
import com.felixgrund.codeshovel.services.impl.CachingRepositoryService;
import com.felixgrund.codeshovel.util.Utl;
import com.felixgrund.codeshovel.wrappers.Commit;
import com.felixgrund.codeshovel.wrappers.StartEnvironment;
import com.google.gson.JsonObject;
import com.shahidul.git.log.oracle.config.AppProperty;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.DiscreteTraceEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
        return "codeShovel";
    }

    @Override
    public String parseChangeType(String rawChangeType) {
        //TODO : convert
        return rawChangeType;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {
        try {
            String repositoryPath = appProperty.getRepositoryBasePath() + "/" + traceEntity.getRepositoryName() + "/.git";
            Repository repository = Utl.createRepository(repositoryPath);
            Git git = new Git(repository);
            RepositoryService repositoryService = new CachingRepositoryService(git, repository, traceEntity.getRepositoryName(), repositoryPath);
            Commit startCommit = repositoryService.findCommitByName(traceEntity.getStartCommitId());

            StartEnvironment startEnv = new StartEnvironment(repositoryService);
            startEnv.setRepositoryPath(repositoryPath);
            startEnv.setFilePath(traceEntity.getFilePath());
            startEnv.setFunctionName(traceEntity.getFunctionName());
            startEnv.setFunctionStartLine(traceEntity.getStartLine());
            startEnv.setStartCommitName(traceEntity.getStartCommitId());
            startEnv.setStartCommit(startCommit);
            startEnv.setFileName(Utl.getFileName(startEnv.getFilePath()));
            //startEnv.setOutputFilePath(outputFilePath);
            Yresult output = ShovelExecution.runSingle(startEnv, startEnv.getFilePath(), true);
            traceEntity.getOutput().put(getTracerName(), DiscreteTraceEntity.builder().commitList(output.entrySet()
                    .stream().map(this::toCommitEntity).toList()).build());
            return traceEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private CommitEntity toCommitEntity(Map.Entry<String, Ychange> commitEntry) {
        com.google.gson.JsonObject json = commitEntry.getValue().toJsonObject();
        CommitEntity.CommitEntityBuilder commitBuilder = CommitEntity.builder();
        if (json.has("commitNameOld")){
            commitBuilder.parentCommitId(json.get("commitNameOld").getAsString());
        }
        commitBuilder.commitId(commitEntry.getKey());
        if (json.has("commitDate")){
            try {
                commitBuilder.commitTime(new SimpleDateFormat().parse(json.get("commitDate").getAsString()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (json.has("type")){
            commitBuilder.changeType(parseChangeType(json.get("type").getAsString()));
        }
        //TODO : source & destination file if (json.has())

        if (json.has("diff")){
            commitBuilder.diff(json.get("diff").getAsString());
        }
      /*  if (json.has("extendedDetails")){
            commitBuilder.diffDetail(json.get("extendedDetails").getAsString());
        }*/
        if (json.has("commitsBetweenForRepo")){
            commitBuilder.commitsBetweenForRepo(json.get("commitsBetweenForRepo").getAsInt());
        }
        if (json.has("commitsBetweenForFile")){
            commitBuilder.commitsBetweenForFile(json.get("commitsBetweenForFile").getAsInt());
        }


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
}
