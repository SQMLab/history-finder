package rnd.method.history.commit.trace.oracle.api.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class RepositoryListResponse {
    String repositoryPath;
    List<String> repositoryList;
}
