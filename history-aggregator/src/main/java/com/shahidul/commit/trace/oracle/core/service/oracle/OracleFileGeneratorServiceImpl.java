package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.storage.StaticTraceDao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.dto.InputCommit;

import java.util.List;

/**
 * @since 3/22/2024
 */
@Service
@AllArgsConstructor
public class OracleFileGeneratorServiceImpl implements OracleFileGeneratorService {
    TraceDao traceDao;
    StaticTraceDao staticTraceDao;



    @Override
    public InputOracle generateFile(String oracleFileName) {
        TraceEntity traceEntity = traceDao.findByOracleName(oracleFileName);
        List<InputCommit> commits = traceEntity.getExpectedCommits().stream()
                .map(commitUdt -> InputCommit.builder()
                        .commitHash(commitUdt.getCommitHash())
                        .changeTags(commitUdt.getChangeTags())
                        .build())
                .toList();
        InputOracle inputOracle = InputOracle.builder()
                .repositoryName(traceEntity.getRepositoryName())
                .repositoryUrl(traceEntity.getRepositoryUrl())
                .startCommitHash(traceEntity.getStartCommitHash())
                .file(traceEntity.getFile())
                .language(traceEntity.getLanguageType().getCode())
                .elementType(traceEntity.getElementType())
                .element(traceEntity.getElementName())
                .startLine(traceEntity.getStartLine())
                .endLine(traceEntity.getEndLine())
                .commits(commits)
                .build();
        return staticTraceDao.save(inputOracle, traceEntity.getOracleFileName());
    }
}
