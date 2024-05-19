package com.shahidul.commit.trace.oracle.cmd;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Shahidul Islam
 * @since 4/27/2024
 */
@Component
@AllArgsConstructor
@Slf4j
public class CtoCommandLineRunner implements CommandLineRunner {
    CtoCommandLineInputParser inputParser;
    CommitTraceShawExportService commitTraceShawExportService;

    @Override
    public void run(String... args) {
        if (args.length > 0) {
            CommandLineInput commandLineInput = inputParser.parse(args);
            log.info("CMD input {}", commandLineInput.getFile());
            commitTraceShawExportService.export(commandLineInput);
        }
    }
}
