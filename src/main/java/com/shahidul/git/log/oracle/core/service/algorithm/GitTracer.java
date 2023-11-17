package com.shahidul.git.log.oracle.core.service.algorithm;

import com.felixgrund.codeshovel.services.impl.CachingRepositoryService;
import com.felixgrund.codeshovel.wrappers.Commit;
import com.shahidul.git.log.oracle.config.AppProperty;
import com.shahidul.git.log.oracle.core.mongo.entity.CommitEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceAnalysisEntity;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Shahidul Islam
 * @since 11/14/2023
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public abstract class GitTracer implements TraceService {

    @Autowired
    AppProperty appProperty;


    public TraceEntity trace(TraceEntity traceEntity, String logCommand) {
        try {
            String repositoryLocation = appProperty.getRepositoryBasePath() + "/" + traceEntity.getRepositoryName();
            Repository repository = new GitServiceImpl().cloneIfNotExists(repositoryLocation, traceEntity.getRepositoryUrl());
            Git git = new Git(repository);
            CachingRepositoryService cachingRepositoryService = new CachingRepositoryService(git, repository, traceEntity.getRepositoryName(), repositoryLocation);


            Runtime runtime = Runtime.getRuntime();
            String[] cmd = {
                    appProperty.getGitShell(),
                    "-c",
                    logCommand
            };


            log.info("Execution ... {}", logCommand);


            Process process = runtime.exec(cmd, null, new File(repositoryLocation));
            process.waitFor(30, TimeUnit.SECONDS );

            Commit startCommit = cachingRepositoryService.findCommitByName(traceEntity.getCommitHash());

            LogCommand logCommandFile = git.log().add(startCommit.getId()).addPath(traceEntity.getFilePath()).setRevFilter(RevFilter.NO_MERGES);
            Iterable<RevCommit> fileRevisions = logCommandFile.call();
            Map<String, Commit> fileHistory = new LinkedHashMap<>();
            for (RevCommit commit : fileRevisions) {
                fileHistory.put(commit.getName(), new Commit(commit));
            }

            List<String> commitNames = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = reader.readLine();
            Pattern COMMIT_NAME_PATTERN = Pattern.compile(".*([a-z0-9]{40}).*");

            StringBuilder shellOutputBuilder = new StringBuilder();
            while (line != null) {
                Matcher matcher = COMMIT_NAME_PATTERN.matcher(line);
                if (matcher.matches() && matcher.groupCount() > 0) {
                    String commitName = matcher.group(1);
                    Commit commit = fileHistory.get(commitName);
                    if (commit != null && commit.getCommitTime() <= startCommit.getCommitTime()) {
                        commitNames.add(commitName);
                    }
                }
                shellOutputBuilder.append(line);
                line = reader.readLine();
            }

            reader.close();
            log.info("Shell output {}", shellOutputBuilder.toString());
            List<CommitEntity> commitEntityList = commitNames.stream()
                    .map(commitHash -> {
                        CommitEntity.CommitEntityBuilder commitEntityBuilder = CommitEntity.builder();
                        if (fileHistory.containsKey(commitHash)) {
                            Commit commit = fileHistory.get(commitHash);
                            commitEntityBuilder.committedAt(commit.getCommitDate())
                                    .author(commit.getAuthorName())
                                    .email(commit.getAuthorEmail())
                                    .message(commit.getCommitMessage());
                        }
                        return commitEntityBuilder
                                .tracerName(getTracerName())
                                .commitHash(commitHash)
                                .build();
                    })
                    .toList();
            traceEntity.getAnalysis().put(getTracerName(), TraceAnalysisEntity.builder().commits(commitEntityList).build());
            return traceEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
