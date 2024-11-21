package com.shahidul.commit.trace.oracle.core.ui;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;

import java.util.List;

public interface GitRepositoryUiService {
    List<String> findRepositoryList();
    List<String> findPathList(String repositoryName, String commitHash, String path);
    List<MethodLocationDto> findMethodLocationList(String repositoryName, String commitHash, String file);
    CommitTraceOutput findMethodHistory(String repositoryName, String commitHash, String file, String methodName, Integer startLine, Integer endLine, TracerName tracerName);
}
