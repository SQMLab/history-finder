package rnd.git.history.finder.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rnd.git.history.finder.enums.LanguageType;

import java.util.List;
import java.util.Map;

/**
 * @since 19/5/24
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonProperty("functionId")
    String methodId;
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
    @JsonProperty("numCommitsSeen")
    Integer analyzedCommitCount;
    @JsonProperty("changeHistory")
    List<String> commitHashes;
    @JsonProperty("changeHistoryShort")
    Map<String, String> commitMap;
    @JsonProperty("changeHistoryDetails")
    Map<String, OutputCommitDetail> commitDetailMap;
    List<InputCommit> commits;
    List<OutputCommitDetail> commitDetails;
}
