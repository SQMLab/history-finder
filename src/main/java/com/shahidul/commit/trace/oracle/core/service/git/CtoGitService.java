package com.shahidul.commit.trace.oracle.core.service.git;

import lombok.SneakyThrows;
import org.eclipse.jgit.revwalk.RevCommit;

public interface CtoGitService {

    @SneakyThrows
    int countCommit(RevCommit startCommit, RevCommit ancestorCommit, String path);
}
