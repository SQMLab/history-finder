package com.shahidul.commit.trace.oracle.cmd.parser;

import com.shahidul.commit.trace.oracle.cmd.model.CommandLineInput;
import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import org.apache.commons.cli.*;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.enums.LanguageType;

/**
 * @author Shahidul Islam
 * @since 2/2/2024
 */
@Service
public class CommandLineInputParserImpl implements CommandLineInputParser {
    @Override
    public CommandLineInput parse(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(createOptions(), args);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String repositoryCacheDirectory = commandLine.getOptionValue("cache-directory");
        String repositoryUrl = commandLine.getOptionValue("repourl");
        String[] urlParts = repositoryUrl.split("/");
        int repositoryNameIndex = repositoryUrl.endsWith(".git") ? urlParts.length - 2 : urlParts.length - 1;
        String tracerNameText = commandLine.getOptionValue("tracername", null);
        return CommandLineInput.builder()
                .command(commandLine.getOptionValue("command"))
                .tracerName(tracerNameText != null ? TracerName.fromCode(tracerNameText) : null)
                .cacheDirectory(repositoryCacheDirectory)
                .repositoryUrl(repositoryUrl)
                .repositoryName(urlParts[repositoryNameIndex])
                .startCommitHash(commandLine.getOptionValue("startcommit", "HEAD"))
                .languageType(LanguageType.valueOf(commandLine.getOptionValue("language", "Java").toUpperCase()))
                .file(commandLine.getOptionValue("file"))
                .methodName(commandLine.getOptionValue("methodname"))
                .startLine(Integer.parseInt(commandLine.getOptionValue("startline")))
                .outputFile(commandLine.getOptionValue("outputfile"))
                .build();
    }

    private Options createOptions() {
        Options options = new Options();

        options.addOption(Option.builder()
                        .longOpt("command")
                        .hasArg(true)
                        .desc("Command name")
                        .required(true)
                        .build())
                .addOption(Option.builder()
                        .longOpt("tracername")
                        .hasArg(true)
                        .desc("Tracer Name")
                        .required(false)
                        .build())
                .addOption(Option.builder()
                        .longOpt("cache-directory")
                        .hasArg(true)
                        .desc("Full path on the local system where repositories will be stored")
                        .required(true)
                        .build())
                .addOption(Option.builder()
                        .longOpt("repourl")
                        .hasArg(true)
                        .desc("Repository URL e.g. github URL")
                        .required(true)
                        .build());


        options.addOption(Option.builder()
                .longOpt("startcommit")
                .hasArg(true)
                .desc(" Relative path file path from the root of repository")
                .required(true)
                .build());

        options.addOption(Option.builder()
                .longOpt("file")
                .hasArg(true)
                .desc(" Relative path file path from the root of repository")
                .required(true)
                .build());

        options.addOption(Option.builder()
                .longOpt("methodname")
                .hasArg(true)
                .desc(" Method name to trace change history")
                .required(true)
                .build());

        options.addOption(Option.builder()
                .longOpt("startline")
                .hasArg(true)
                .desc(" Start line number of the method")
                .required(true)
                .build());
        options.addOption(Option.builder()
                .longOpt("language")
                .hasArg(true)
                .desc(" Programming Language the method is written in")
                .required(false)
                .build());
        options.addOption(Option.builder()
                .longOpt("outputfile")
                .hasArg(true)
                .desc(" Path to write output")
                .required(false)
                .build());
        return options;
    }
}
