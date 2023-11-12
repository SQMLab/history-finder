package com.shahidul.git.log.oracle.core.model;

import lombok.*;

import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GitLog {
    String repositoryName;
    String repositoryWebURL;
    String startCommitId;
    String filePath;
    String functionName;
    String functionKey;
    Integer functionStartLine;
    List<GitCommit> expectedChanges;
}
