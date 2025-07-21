package rnd.git.history.finder.dto;

import lombok.*;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;


/**
 * @since 11/6/24
 **/
@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class MethodHolder implements Cloneable {
    private String commitHash;
    private String file;
    @Setter
    private FileChangeDto fileChangeDto;
    private MethodSourceInfo methodSourceInfo;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return MethodHolder.builder()
                .commitHash(commitHash)
                .file(file)
                .methodSourceInfo(methodSourceInfo.clone())
                .build();
    }
}
