package com.shahidul.commit.trace.oracle.cmd;

import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
public interface CommandLineHelperService {
    InputOracle toInputOracle(CommandLineInput commandLineInput);
    TraceEntity loadOracle(InputOracle inputOracle);
}
