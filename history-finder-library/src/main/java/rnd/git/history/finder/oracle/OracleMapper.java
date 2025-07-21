package rnd.git.history.finder.oracle;

import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.dto.InputOracle;

/**
 * @since 23/5/24
 **/
public interface OracleMapper {
    HistoryFinderInput toHistoryFinderInput(InputOracle inputOracle, String cacheDirectory);
}
