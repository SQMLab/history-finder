package com.shahidul.commit.trace.oracle.core.ui;

import java.util.List;

public interface GitRepositoryUiService {
    List<String> findRepositoryList();
    List<String> findPathList(String repositoryName, String commitHash, String path);
}
