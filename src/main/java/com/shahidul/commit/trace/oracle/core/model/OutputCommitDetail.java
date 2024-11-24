package com.shahidul.commit.trace.oracle.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shahidul.commit.trace.oracle.core.enums.ChangeTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OutputCommitDetail {
    @JsonProperty("commitName")
    String commitHash;
    @JsonProperty("commitDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yy HH:mm a")
    Date committedAt;
    @JsonIgnore
    Integer startLine;
    @JsonIgnore
    Integer endLine;
    @JsonIgnore
    String newFile;
    @JsonIgnore
    String oldFile;
    @JsonIgnore
    List<ChangeTag> changeTags;
    List<String> displayChangeTags;
    @JsonProperty("type")
    String changeTagText;
    @JsonProperty("commitAuthor")
    String author;
    String authorSearchUrl;
    @JsonIgnore
    String email;
    @JsonProperty("commitMessage")
    String shortMessage;
    @JsonIgnore
    String fullMessage;
    String commitUrl;
    String diffUrl;
    String oldFilUrl;
    String newFileUrl;
    String diff;
    @JsonIgnore
    String docDiff;
    @JsonIgnore
    String diffDetail;
}
