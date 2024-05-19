package com.shahidul.commit.trace.oracle.cmd;

import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.dao.TraceDao;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.helper.OracleHelperService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
@Component
@AllArgsConstructor
public class CommandLineHelperServiceImpl implements CommandLineHelperService {
    OracleHelperService oracleHelperService;
    TraceDao traceDao;
    @Override
    public InputOracle toInputOracle(CommandLineInput commandLineInput) {
        return InputOracle.builder()
                .repositoryUrl(commandLineInput.getRepositoryUrl())
                .repositoryName(commandLineInput.getRepositoryName())
                .startCommitHash(commandLineInput.getStartCommitHash())
                .file(commandLineInput.getFile())
                .language(commandLineInput.getLanguageType().name())
                .elementType("method")
                .element(commandLineInput.getMethodName())
                .startLine(commandLineInput.getStartLine())
                .endLine(null)
                .commits(new ArrayList<>())
                .build();
    }

    @Override
    public TraceEntity loadOracle(InputOracle inputOracle) {
        String oracleHash = oracleHelperService.generateOracleHash(inputOracle);
        TraceEntity traceEntity = traceDao.findByOracleHash(oracleHash);
        if (traceEntity == null){
            return oracleHelperService.build(inputOracle);
        }else {
            return traceEntity;
        }
    }
}
