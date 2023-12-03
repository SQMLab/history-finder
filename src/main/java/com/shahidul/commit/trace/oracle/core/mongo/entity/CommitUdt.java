package com.shahidul.commit.trace.oracle.core.mongo.entity;

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
public class CommitUdt implements Cloneable{
    String tracerName;
    String commitHash;
    String parentCommitHash;
    Date committedAt;
    String author;
    String email;
    String shortMessage;
    String fullMessage;
    String changeType;
    String renamedElement;
    String renamedFile;
    String diff;
    String diffDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
