package rnd.method.history.commit.trace.oracle.core.service.oracle;

import rnd.method.history.commit.trace.oracle.core.model.InputOracle;

/**
 * @since 3/22/2024
 */
public interface OracleFileGeneratorService {
    InputOracle generateFile(String oracleFileName);
}
