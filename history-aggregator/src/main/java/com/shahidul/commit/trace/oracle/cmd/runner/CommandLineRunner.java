package com.shahidul.commit.trace.oracle.cmd.runner;

import com.github.javaparser.utils.Log;
import com.shahidul.commit.trace.oracle.cmd.exporter.CommitTraceDetailExportService;
import com.shahidul.commit.trace.oracle.cmd.exporter.CommitTraceComparisonExportService;
import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.cmd.parser.CommandLineInputParser;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

/**
 * @author Shahidul Islam
 * @since 4/27/2024
 */
@Component
@AllArgsConstructor
@Slf4j
public class CommandLineRunner implements org.springframework.boot.CommandLineRunner {
    AppProperty appProperty;
    CommandLineInputParser inputParser;
    CommitTraceComparisonExportService commitTraceComparisonExportService;
    CommitTraceDetailExportService commitTraceDetailExportService;
    ConfigurableApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        if (args.length > 0) {
            CommandLineInput commandLineInput = inputParser.parse(args);
            log.info("CMD input {}", commandLineInput.getFile());
            String command = commandLineInput.getCommand();
            if ("commit-trace-detail".equalsIgnoreCase(command)) {
                commitTraceDetailExportService.export(commandLineInput);
            } else if ("commit-trace-comparison".equalsIgnoreCase(command)) {
                commitTraceComparisonExportService.export(commandLineInput);
            } else throw new RuntimeException("Invalid command");
            applicationContext.close();
        } else {
            String url = "http://localhost:" + appProperty.getServerPort();
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    Runtime.getRuntime().exec("xdg-open " + url);
                }
            } catch (Exception e) {
                Log.info("Server is running open browser and browse URL: " + url);
            }
        }
    }
}
