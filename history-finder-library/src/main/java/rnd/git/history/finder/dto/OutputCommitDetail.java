package rnd.git.history.finder.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @since 19/5/24
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputCommitDetail {
    @JsonProperty("commitName")
    String commitHash;
    String parentCommitHash;
    String ancestorCommitHash;
    @JsonProperty("commitDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yy HH:mm a")
    Date committedAt;
    @JsonIgnore
    Integer startLine;
    @JsonIgnore
    Integer endLine;
    @JsonProperty("path")
    String newFile;
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
    String oldFileUrl;
    String newFileUrl;
    @JsonProperty("daysBetweenCommits")
    Double daysBetweenCommits;
    @JsonProperty("commitsBetweenForRepo")
    Integer commitCountBetweenForRepo;
    @JsonProperty("commitsBetweenForFile")
    Integer commitCountBetweenForFile;
    @JsonProperty("extendedDetails")
    AdditionalCommitInfo additionalCommitInfo;
    @JsonProperty("actualSource")
    String newCode;
    String newDoc;
    String diff;
    @JsonIgnore
    String docDiff;
    @JsonIgnore
    String diffDetail;
    @JsonProperty("subchanges")
    List<OutputCommitDetail> subChangeList;
}
