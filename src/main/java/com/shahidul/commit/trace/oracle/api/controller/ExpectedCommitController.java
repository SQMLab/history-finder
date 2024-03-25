package com.shahidul.commit.trace.oracle.api.controller;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.service.oracle.ExpectedCommitService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Shahidul Islam
 * @since 3/19/2024
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/oracle/commit")
public class ExpectedCommitController {
    ExpectedCommitService expectedCommitService;

    @GetMapping("/detail")
    public CommitUdt commitDetail(@RequestParam String oracleFileName, @RequestParam String commitHash, @RequestParam(required = false) TracerName fromTracer) {
        return expectedCommitService.findCommit(oracleFileName, commitHash, fromTracer == null ? TracerName.EXPECTED : fromTracer);
    }

    @GetMapping("/add")
    public CommitUdt addCommit(@RequestParam String oracleFileName, @RequestParam String commitHash, @RequestParam TracerName fromTracer) {
        return expectedCommitService.addCommit(oracleFileName, commitHash, fromTracer);
    }

    @GetMapping("/delete")
    public CommitUdt deleteCommit(@RequestParam String oracleFileName, @RequestParam String commitHash) {
        return expectedCommitService.deleteCommit(oracleFileName, commitHash);
    }


}
