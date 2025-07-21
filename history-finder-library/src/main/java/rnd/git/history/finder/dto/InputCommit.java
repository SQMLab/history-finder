package rnd.git.history.finder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @since 11/10/2023
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InputCommit {
    String commitHash;
    List<ChangeTag> changeTags;
}
