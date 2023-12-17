package com.shahidul.commit.trace.oracle.core.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author Shahidul Islam
 * @since 11/10/2023
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommitUdt implements Cloneable{
    String tracerName;
    String commitHash;
    String parentCommitHash;
    Date committedAt;
    Integer startLine;
    Integer endLine;
    String codeFragment;
    List<String> changeList;
    String filePath;
    String author;
    String email;
    String shortMessage;
    String fullMessage;
    String changeType;
    String renamedElement;
    String diff;
    String diffDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
