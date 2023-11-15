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
public class Trace {
    String repositoryName;
    String repositoryUrl;
    String commitHash;
    String filePath;
    String elementType;
    String elementName;
    Integer startLine;
    Integer endLine;
    List<Commit> expectedCommits;
}
