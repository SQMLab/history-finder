package com.shahidul.commit.trace.oracle.core.ui;

import com.shahidul.commit.trace.oracle.api.payload.RepositoryListResponse;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import rnd.git.history.finder.dto.CommitTraceOutput;
import com.shahidul.commit.trace.oracle.core.model.HistoryInputParam;
import com.shahidul.commit.trace.oracle.core.ui.dto.RepositoryCheckoutResponse;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;

import java.util.List;

public interface GitRepositoryUiService {
    RepositoryListResponse findRepositoryList();

    List<String> findPathList(String repositoryPath,
                              String repositoryName,
                              String startCommitHash,
                              String path);

    List<MethodLocationDto> findMethodLocationList(String repositoryPath,
                                                   String repositoryName,
                                                   String commitHash,
                                                   String file);

    CommitTraceOutput findMethodHistory(String repositoryHostName,
                                        String repositoryAccountName,
                                        String repositoryPath,
                                        String repositoryName,
                                        String commitHash,
                                        String file,
                                        String methodName,
                                        Integer startLine,
                                        Integer endLine,
                                        TracerName tracerName,
                                        boolean forceExecute);

    RepositoryCheckoutResponse checkoutRepository(String location);

    List<String> getOracleFileList();

    HistoryInputParam findOracleMethodHistory(String file, TracerName tracerName);

}
