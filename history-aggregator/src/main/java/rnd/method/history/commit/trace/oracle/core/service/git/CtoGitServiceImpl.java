package rnd.method.history.commit.trace.oracle.core.service.git;

import lombok.SneakyThrows;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;

public class CtoGitServiceImpl implements CtoGitService {
    private Git git;
    private Repository repository;
    public CtoGitServiceImpl(Repository repository) {
        this.git = new Git(repository);
        this.repository = repository;
    }

    @SneakyThrows
    @Override
    public int countCommit(RevCommit startCommit, RevCommit ancestorCommit, String path) {
        LogCommand logCommand = git.log()
                .add(createCommitObjectId(startCommit.getName()));
        if (path != null) {
            logCommand.addPath(path);
        }

        int commitCount = 0;
        for (RevCommit revCommit : logCommand.call()) {
            if (revCommit.getName().equalsIgnoreCase(ancestorCommit.getName())
                    || revCommit.getCommitTime() < ancestorCommit.getCommitTime()) {
                break;
            }
            commitCount += 1;
        }

        return commitCount;
    }
    @Override
    public ObjectId createCommitObjectId(String commitHashOrHead) throws IOException {
        if ("HEAD".equalsIgnoreCase(commitHashOrHead)) {
            return git.getRepository().resolve(Constants.HEAD);
        } else {
            return ObjectId.fromString(commitHashOrHead);
        }
    }

    @Override
    public String resolveAsCommitHash(String commitHashOrHead) throws IOException {
        if ("HEAD".equalsIgnoreCase(commitHashOrHead)) {
            return git.getRepository().resolve(Constants.HEAD).getName();
        } else {
            return commitHashOrHead;
        }
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

}
