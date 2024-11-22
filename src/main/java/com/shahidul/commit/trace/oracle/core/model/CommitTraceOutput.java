package com.shahidul.commit.trace.oracle.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shahidul.commit.trace.oracle.core.enums.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommitTraceOutput {
    @JsonProperty("origin")
    String tracerName;
    String displayTracerName;
    String repositoryName;
    String repositoryUrl;
    @JsonProperty("repositoryPath")
    String repositoryFile;
    @JsonProperty("startCommitName")
    String startCommitHash;
    @JsonProperty("sourceFilePath")
    String file;
    @JsonProperty("sourceFileName")
    String fileName;
    @JsonIgnore
    LanguageType language;
    @JsonIgnore
    String elementType;
    @JsonProperty("functionName")
    String element;
    @JsonProperty("functionStartLine")
    Integer startLine;
    @JsonProperty("functionEndLine")
    Integer endLine;

    @JsonIgnore
    Double precision;
    @JsonIgnore
    Double recall;
    @JsonProperty("timeTaken")
    Long runtime;
    @JsonProperty("changeHistory")
    List<String> commitHashes;
    @JsonIgnore
    List<InputCommit> commits;
    @JsonProperty("changeHistoryShort")
    Map<String, String> commitMap;
    @JsonIgnore
    List<OutputCommitDetail> commitDetails;
    @JsonProperty("changeHistoryDetails")
    Map<String, OutputCommitDetail> commitDetailMap;
}
