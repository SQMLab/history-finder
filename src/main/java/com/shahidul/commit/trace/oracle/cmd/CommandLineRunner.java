package com.shahidul.commit.trace.oracle.cmd;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.cmd.parser.CommandLineInputParser;
import com.shahidul.commit.trace.oracle.cmd.exporter.CommitTraceDetailExportService;
import com.shahidul.commit.trace.oracle.cmd.exporter.CommitTraceShawExportService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Shahidul Islam
 * @since 4/27/2024
 */
@Component
@AllArgsConstructor
@Slf4j
public class CommandLineRunner implements org.springframework.boot.CommandLineRunner {
    CommandLineInputParser inputParser;
    CommitTraceShawExportService commitTraceShawExportService;
    CommitTraceDetailExportService commitTraceDetailExportService;
    ConfigurableApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        if (args.length > 0) {
            CommandLineInput commandLineInput = inputParser.parse(args);
            log.info("CMD input {}", commandLineInput.getFile());
            String command = commandLineInput.getCommand();
            if ("ctd".equalsIgnoreCase(command)) {
                commitTraceDetailExportService.export(commandLineInput);
            } else if ("cts".equalsIgnoreCase(command)) {
                commitTraceShawExportService.export(commandLineInput);
            } else throw new RuntimeException("Invalid command");
            applicationContext.close();
        }
    }
}
