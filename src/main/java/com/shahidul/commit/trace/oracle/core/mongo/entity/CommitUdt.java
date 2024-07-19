package com.shahidul.commit.trace.oracle.core.mongo.entity;

import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

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
    LinkedHashSet<ChangeTag> changeTags;
    String oldFile;
    String newFile;
    Integer fileRenamed;
    Integer fileMoved;
    String oldElement;
    String newElement;
    String author;
    String email;
    String shortMessage;
    String fullMessage;
    String diffUrl;
    String oldFilUrl;
    String newFileUrl;
    String codeFragment;
    String documentation;
    //@Transient
    String diff;
    String docDiff;
    @Transient
    String diffDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
