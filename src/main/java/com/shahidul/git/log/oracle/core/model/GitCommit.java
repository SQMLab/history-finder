package com.shahidul.git.log.oracle.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GitCommit {
    String parentCommitId;
    String commitId;
    Long commitTime;
    String changeType;
    String elementFileBefore;
    String elementFileAfter;
    String elementNameBefore;
    String elementNameAfter;
}
