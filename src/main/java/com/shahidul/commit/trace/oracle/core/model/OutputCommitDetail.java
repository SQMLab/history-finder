package com.shahidul.commit.trace.oracle.core.model;

import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.LinkedHashSet;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OutputCommitDetail {
    String commitHash;
    Date committedAt;
    Integer startLine;
    Integer endLine;
    String file;
    LinkedHashSet<ChangeTag> changeTags;
    String email;
    String shortMessage;
    String fullMessage;
    String diffUrl;
    String diff;
    String docDiff;
    String diffDetail;
}
