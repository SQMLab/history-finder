package rnd.method.history.commit.trace.oracle.core.ui.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MethodLocationDto {
    String methodName;
    String signature;
    Integer startLine;
    Integer endLine;
}
