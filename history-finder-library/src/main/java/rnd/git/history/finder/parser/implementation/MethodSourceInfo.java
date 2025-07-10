package rnd.git.history.finder.parser.implementation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodSourceInfo implements Cloneable {

    @JsonIgnore
    private MethodDeclaration methodDeclaration;

    @Builder.Default
    private String methodRawSourceCode = "";

    @Builder.Default
    private String annotation = "";

    @Builder.Default
    private String fullCode = "";

    @Builder.Default
    private int startLine = 0;
    @Builder.Default
    private int endLine = 0;

    private long simHash1;
    private long simHash2;


    @Override
    public MethodSourceInfo clone() {
        return MethodSourceInfo.builder()
                .methodDeclaration(methodDeclaration.clone())
                .methodRawSourceCode(methodRawSourceCode)
                .fullCode(fullCode)
                .annotation(annotation)
                .startLine(startLine)
                .endLine(endLine)
                .simHash1(simHash1)
                .simHash2(simHash2)
                .build();
    }
}
