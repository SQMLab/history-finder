package com.shahidul.git.log.oracle.core.service;

import com.shahidul.git.log.oracle.config.AppProperty;
import com.shahidul.git.log.oracle.core.enums.TrackerName;
import com.shahidul.git.log.oracle.core.mongo.entity.TraceEntity;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

/**
 * @author Shahidul Islam
 * @since 11/14/2023
 */
public class GitMethodRegxTracer implements TraceService {
    private static final GitServiceImpl gitService = new GitServiceImpl();
    AppProperty appProperty;

    @Override
    public String getTracerName() {
        return TrackerName.GIT_REGEX.getCode();
    }

    @Override
    public String parseChangeType(String rawChangeType) {
        return rawChangeType;
    }

    @Override
    public TraceEntity trace(TraceEntity traceEntity) {
/*        String repositoryWebURL = traceEntity.getRepositoryUrl();
        String repositoryName = repositoryWebURL.replace("https://github.com/", "").replace(".git", "").replace("/", "\\");
        String projectDirectory = appProperty.getRepositoryBasePath() + repositoryName;

        try (Repository repository = gitService.cloneIfNotExists(projectDirectory, repositoryWebURL)) {
            try (Git git = new Git(repository)) {
                git.log()
                        .add(repository.resolve(traceEntity.getCommitHash()))
                        .addPath(traceEntity.getFilePath())
                        .setRevFilter(RevFilter.NO_MERGES)
                        .

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/


        return null;
    }
}
