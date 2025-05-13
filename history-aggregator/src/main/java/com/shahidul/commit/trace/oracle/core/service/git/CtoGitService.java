package com.shahidul.commit.trace.oracle.core.service.git;

import lombok.SneakyThrows;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;

public interface CtoGitService {

    @SneakyThrows
    int countCommit(RevCommit startCommit, RevCommit ancestorCommit, String path);
    ObjectId createCommitObjectId(String commitHashOrHead) throws IOException;
    String resolveAsCommitHash(String commitHashOrHead) throws IOException;
    Repository getRepository();
}
