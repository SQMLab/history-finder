package rnd.git.history.finder.oracle;

import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.dto.InputOracle;
import rnd.git.history.finder.enums.LanguageType;

/**
 * @author Shahidul Islam
 * @since 23/5/24
 **/
public class OracleMapperImpl implements OracleMapper {
    @Override
    public HistoryFinderInput toHistoryFinderInput(InputOracle inputOracle, String cacheDirectory) {
        return HistoryFinderInput.builder()
                .cacheDirectory(cacheDirectory)
                .repositoryUrl(inputOracle.getRepositoryUrl())
                .startCommitHash(inputOracle.getStartCommitHash())
                .repositoryName(inputOracle.getRepositoryName())
                .languageType(LanguageType.from(inputOracle.getLanguage()))
                .file(inputOracle.getFile())
                .methodName(inputOracle.getElement())
                .startLine(inputOracle.getStartLine())
                .build();
    }
}
