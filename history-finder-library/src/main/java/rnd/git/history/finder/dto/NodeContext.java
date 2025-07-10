package rnd.git.history.finder.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NodeContext {
    String commitHash;
    String file;
    String methodName;
    String fullMethodSignature;
    Integer startLine;
}
