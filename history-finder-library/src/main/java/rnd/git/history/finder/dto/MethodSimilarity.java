package rnd.git.history.finder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;

/**
 * @since 20/7/24
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodSimilarity {
    MethodSourceInfo methodSourceInfo;
    Double signatureSimilarity;
    Double javaDocSimilarity;
    Double annotationSimilarity;
    Double codeBlockSimilarity;
    Double overallSimilarity;
}
