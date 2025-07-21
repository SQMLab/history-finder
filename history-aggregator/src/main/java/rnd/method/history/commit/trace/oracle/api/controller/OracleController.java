package rnd.method.history.commit.trace.oracle.api.controller;

import rnd.method.history.commit.trace.oracle.core.model.InputOracle;
import rnd.method.history.commit.trace.oracle.core.mongo.entity.TraceEntity;
import rnd.method.history.commit.trace.oracle.core.service.oracle.OracleFileGeneratorService;
import rnd.method.history.commit.trace.oracle.core.service.oracle.OracleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @since 3/22/2024
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/oracle")
public class OracleController {
    OracleFileGeneratorService oracleFileGeneratorService;
    OracleService oracleService;

    @GetMapping("/delete")
    public TraceEntity deleteOracle(@RequestParam String oracleFileName) {
        return oracleService.deleteOracle(oracleFileName);
    }

    @GetMapping("/generate-file")
    public InputOracle generateFile(@RequestParam String oracleFileName) {
        return oracleFileGeneratorService.generateFile(oracleFileName);
    }
}
