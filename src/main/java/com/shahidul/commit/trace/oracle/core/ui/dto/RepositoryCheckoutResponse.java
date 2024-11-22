package com.shahidul.commit.trace.oracle.core.ui.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RepositoryCheckoutResponse {
    String host;
    String accountName;
    String repositoryName;
    String path;
}
