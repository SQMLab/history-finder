package com.shahidul.commit.trace.oracle.core.service.algorithm;

import com.felixgrund.codeshovel.services.impl.CachingRepositoryService;
import com.felixgrund.codeshovel.wrappers.Commit;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.util.Util;
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

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private static final String DIVIDER = "#######--------------------------#######";
    protected static final String LOG_FORMAT = "--pretty=format:" + DIVIDER + "%n%H%n" + DIVIDER;


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
            process.waitFor(30, TimeUnit.SECONDS);

            Commit startCommit = cachingRepositoryService.findCommitByName(traceEntity.getStartCommitHash());

            LogCommand logCommandFile = git.log().add(startCommit.getId()).addPath(traceEntity.getFile()).setRevFilter(RevFilter.NO_MERGES);
            Iterable<RevCommit> fileRevisions = logCommandFile.call();
            Map<String, Commit> fileHistory = new LinkedHashMap<>();
            for (RevCommit commit : fileRevisions) {
                fileHistory.put(commit.getName(), new Commit(commit));
            }

            List<CommitUdt> commitUdtList = new ArrayList<>();

            Scanner scanner = new Scanner(process.getInputStream());
            scanner.useDelimiter(DIVIDER);
            String parentCommitHash = traceEntity.getStartCommitHash();
            while (scanner.hasNext()) {
                String commitHash = scanner.next().strip();
                CommitUdt.CommitUdtBuilder commitEntityBuilder = CommitUdt.builder();
                if (fileHistory.containsKey(commitHash)) {
                    Commit commit = fileHistory.get(commitHash);
                    commitEntityBuilder.committedAt(commit.getCommitDate())
                            .author(commit.getAuthorName())
                            .email(commit.getAuthorEmail())
                            .shortMessage(commit.getCommitMessage());
                }
                String diff = scanner.next().strip();
                String[] diffHeaderParts = diff.substring(0, diff.indexOf("\n")).split(" ");
                String oldFile = diffHeaderParts[diffHeaderParts.length - 2].substring(2);
                String newFile = diffHeaderParts[diffHeaderParts.length - 1].substring(2);
                commitUdtList.add(commitEntityBuilder
                        .tracerName(getTracerName())
                        .parentCommitHash(parentCommitHash)
                        .commitHash(commitHash)
                        .changeTags(Collections.emptyList())
                        .oldFile(oldFile)
                        .newFile(newFile)
                        .fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                        .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0)
                        .diff(diff.substring(diff.indexOf("@@")))
                        .build());
                parentCommitHash = commitHash;
            }
            scanner.close();

         /*
         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> commitNames = new ArrayList<>();
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
                 reader.close();
            }*/


            traceEntity.getAnalysis().put(getTracerName(), AnalysisUdt.builder().commits(commitUdtList).build());
            return traceEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
