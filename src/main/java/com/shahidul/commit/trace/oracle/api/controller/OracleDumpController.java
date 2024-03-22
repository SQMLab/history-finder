package com.shahidul.commit.trace.oracle.api.controller;

import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.core.service.oracle.OracleDumpService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Shahidul Islam
 * @since 3/22/2024
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/oracle")
public class OracleDumpController {
    OracleDumpService oracleDumpService;
    @GetMapping("/dump")
    public TraceEntity dumpOracle(@RequestParam Integer oracleFileName) {
        return oracleDumpService.dumpOracle(oracleFileName);
    }
}
