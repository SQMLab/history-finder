package rnd.method.history.commit.trace.oracle.core.service.algorithm;

import com.felixgrund.codeshovel.services.impl.CachingRepositoryService;
import com.felixgrund.codeshovel.wrappers.Commit;
import rnd.method.history.commit.trace.oracle.config.AppProperty;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.AnalysisUdt;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.CommitUdt;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;
import rnd.method.history.commit.trace.oracle.util.Util;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
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
            String repositoryLocation = Util.getLocalProjectDirectory(traceEntity.getCloneDirectory(), appProperty.getRepositoryBasePath(), traceEntity.getRepositoryName());
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


//            Process process = runtime.exec(cmd, null, new File(repositoryLocation));
            Process process = new ProcessBuilder(cmd)
                    .directory(new File(repositoryLocation))
//                    .redirectErrorStream(true)
//                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start();

            StringBuilder outputBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append(System.lineSeparator());
                }
            }

            boolean isTimeout = !process.waitFor(120, TimeUnit.SECONDS);

            if (isTimeout) {
                process.destroyForcibly();
            }
            Commit startCommit = cachingRepositoryService.findCommitByName(traceEntity.getStartCommitHash());

            LogCommand logCommandFile = git.log().add(startCommit.getId()).addPath(traceEntity.getFile()).setRevFilter(RevFilter.NO_MERGES);
            Iterable<RevCommit> fileRevisions = logCommandFile.call();
            Map<String, Commit> fileHistory = new LinkedHashMap<>();
            for (RevCommit commit : fileRevisions) {
                fileHistory.put(commit.getName(), new Commit(commit));
            }

            List<CommitUdt> commitUdtList = new ArrayList<>();

            //Scanner scanner = new Scanner(process.getInputStream());
            Scanner scanner = new Scanner(outputBuilder.toString());
            scanner.useDelimiter(DIVIDER);
            while (scanner.hasNext()) {
                String commitHash = scanner.next().strip();

                RevCommit revCommit = cachingRepositoryService.findRevCommitById(repository.resolve(commitHash));
                //TODO: How to link if more than one parent commit?
                RevCommit revParentCommit = revCommit.getParent(0);
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
                String oldFile = scanner.hasNext() ? diffHeaderParts[diffHeaderParts.length - 2].substring(2) : null;
                String newFile = diffHeaderParts[diffHeaderParts.length - 1].substring(2);
                if (oldFile != null) {
                    commitEntityBuilder.fileRenamed(Util.isFileRenamed(oldFile, newFile) ? 1 : 0)
                            .fileMoved(Util.isFileMoved(oldFile, newFile) ? 1 : 0);
                }
                commitUdtList.add(commitEntityBuilder
                        .tracerName(getTracerName())
                        .parentCommitHash(revParentCommit.getName())
                        .commitHash(commitHash)
                        .changeTags(Collections.emptyList())
                        .oldFile(oldFile)
                        .newFile(newFile)
                        .diff(diff.substring(diff.indexOf("@@")))
                        .build());
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
