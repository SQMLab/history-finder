package rnd.method.history.commit.trace.oracle.core.ui;

import rnd.method.history.commit.trace.oracle.api.payload.RepositoryListResponse;
import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import rnd.git.history.finder.dto.CommitTraceOutput;
import rnd.method.history.commit.trace.oracle.core.model.HistoryInputParam;
import rnd.method.history.commit.trace.oracle.core.ui.dto.RepositoryCheckoutResponse;
import rnd.method.history.commit.trace.oracle.core.ui.dto.MethodLocationDto;

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

    CommitTraceOutput findMethodHistory(String repositoryUrl,
                                        String commitHash,
                                        String file,
                                        String methodName,
                                        Integer startLine,
                                        Integer endLine,
                                        TracerName tracerName,
                                        boolean useCache);

    RepositoryCheckoutResponse checkoutRepository(String location);

    List<String> getOracleFileList();

    HistoryInputParam findOracleMethodHistory(String file, TracerName tracerName);

}
