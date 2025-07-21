package com.shahidul.commit.trace.oracle.api.controller;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.service.oracle.ExpectedCommitService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rnd.git.history.finder.dto.ChangeTag;

import java.util.List;

/**
 * @since 3/19/2024
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/oracle/commit")
public class ExpectedCommitController {
    ExpectedCommitService expectedCommitService;

    @GetMapping("/detail")
    public CommitUdt commitDetail(@RequestParam String oracleFileName, @RequestParam String commitHash, @RequestParam(required = false) String fromTracer) {
        return expectedCommitService.findCommit(oracleFileName, commitHash, fromTracer == null ? TracerName.EXPECTED : TracerName.fromCode(fromTracer));
    }

    @GetMapping("/add")
    public CommitUdt addCommit(@RequestParam String oracleFileName, @RequestParam String commitHash, @RequestParam String fromTracer) {
        return expectedCommitService.addCommit(oracleFileName, commitHash, TracerName.fromCode(fromTracer));
    }

    @GetMapping("/tag/update")
    public CommitUdt updateTags(@RequestParam String oracleFileName, @RequestParam String commitHash, @RequestParam String fromTracer, @RequestParam List<ChangeTag> changeTags) {
        return expectedCommitService.updateTags(oracleFileName, commitHash, TracerName.fromCode(fromTracer), changeTags);
    }

    @GetMapping("/delete")
    public CommitUdt deleteCommit(@RequestParam String oracleFileName, @RequestParam String commitHash) {
        return expectedCommitService.deleteCommit(oracleFileName, commitHash);
    }


}
