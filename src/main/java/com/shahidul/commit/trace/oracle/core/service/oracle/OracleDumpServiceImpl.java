package com.shahidul.commit.trace.oracle.core.service.oracle;

import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Shahidul Islam
 * @since 3/22/2024
 */
@Service
@AllArgsConstructor
public class OracleDumpServiceImpl implements OracleDumpService {
    @Override
    public TraceEntity dumpOracle(Integer oracleFileName) {
        return null;
    }
}
