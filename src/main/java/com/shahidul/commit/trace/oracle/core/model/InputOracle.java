package com.shahidul.commit.trace.oracle.core.model;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InputOracle {
    String repositoryName;
    String repositoryUrl;
    String startCommitHash;
    String file;
    String elementType;
    String element;
    Integer startLine;
    Integer endLine;
    List<InputCommit> expectedCommits;
    Map<String, InputTrace> analyzer = new HashMap<>();
}
