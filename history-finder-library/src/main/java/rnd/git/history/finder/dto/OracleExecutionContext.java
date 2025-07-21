package rnd.git.history.finder.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * @since 23/5/24
 **/
@Data
@Builder
public class OracleExecutionContext {
    InputOracle inputOracle;
    HistoryFinderInput historyFinderInput;
    Set<String> outputCommitSet;
}
