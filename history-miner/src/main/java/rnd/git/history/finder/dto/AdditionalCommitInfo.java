package rnd.git.history.finder.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalCommitInfo {
    @JsonIgnore
    ChangeTag changeTag;

    @JsonProperty("oldMethodName")
    String oldMethodName;

    @JsonProperty("newMethodName")
    String newMethodName;

    @JsonProperty("oldValue")
    String oldSignature;

    @JsonProperty("newValue")
    String newSignature;

    @JsonProperty("oldPath")
    String oldFile;

    @JsonProperty("newPath")
    String newFile;
}
