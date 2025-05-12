package rnd.git.history.finder.oracle;

import rnd.git.history.finder.dto.InputOracle;

/**
 * @author Shahidul Islam
 * @since 23/5/24
 **/
public interface OracleReader {
    InputOracle readFromOracle(Integer oracleId);
}
