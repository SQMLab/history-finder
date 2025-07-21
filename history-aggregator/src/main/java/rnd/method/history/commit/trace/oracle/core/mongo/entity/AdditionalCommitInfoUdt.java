package rnd.method.history.commit.trace.oracle.core.mongo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rnd.git.history.finder.dto.ChangeTag;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AdditionalCommitInfoUdt {
    ChangeTag changeTag;
    String oldMethodName;
    String newMethodName;
    String oldSignature;
    String newSignature;
    String oldFile;
    String newFile;
}
