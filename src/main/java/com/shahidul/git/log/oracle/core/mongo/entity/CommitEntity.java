package com.shahidul.git.log.oracle.core.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommitEntity {
    String tracerName;
    String parentCommitHash;
    String commitHash;
    Date commitTime;
    Date parentCommitTime;
    String changeType;
    String elementFileBefore;
    String elementFileAfter;
    String elementNameBefore;
    String elementNameAfter;
    String diff;
    String diffDetail;
    String author;
    String parentAuthor;
    String commitMessage;
    Integer commitsBetweenForRepo;
    Integer commitsBetweenForFile;

}
