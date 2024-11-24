package com.shahidul.commit.trace.oracle.api.payload;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class RepositoryListResponse {
    String repositoryPath;
    List<String> repositoryList;
}
