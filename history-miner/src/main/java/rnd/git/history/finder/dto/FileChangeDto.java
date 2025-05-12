package rnd.git.history.finder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.jgit.diff.DiffEntry;

/**
 * @author Shahidul Islam
 * @since 12/7/24
 **/
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileChangeDto {
    String oldFile;
    String newFile;
    Double matching;
    DiffEntry.ChangeType changeType;

}
