package com.shahidul.commit.trace.oracle.core.ui;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.ui.dto.RepositoryCheckoutResponse;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;

import java.util.List;

public interface GitRepositoryUiService {
    List<String> findRepositoryList();
    List<String> findPathList(String repositoryPath, String repositoryName, String commitHash, String path);
    List<MethodLocationDto> findMethodLocationList(String repositoryPath, String repositoryName, String commitHash, String file);
    CommitTraceOutput findMethodHistory(String repositoryHostName,
                                        String repositoryAccountName,
                                        String repositoryPath,
                                        String repositoryName,
                                        String commitHash,
                                        String file,
                                        String methodName,
                                        Integer startLine,
                                        Integer endLine,
                                        TracerName tracerName);
    RepositoryCheckoutResponse checkoutRepository(String location);
}
