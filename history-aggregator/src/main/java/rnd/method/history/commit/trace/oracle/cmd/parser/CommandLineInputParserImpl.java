package rnd.method.history.commit.trace.oracle.cmd.parser;

import lombok.AllArgsConstructor;
import rnd.method.history.commit.trace.oracle.cmd.model.CommandLineInput;
import rnd.method.history.commit.trace.oracle.config.AppProperty;
import rnd.method.history.commit.trace.oracle.core.enums.TracerName;
import rnd.method.history.commit.trace.oracle.util.Util;
import org.apache.commons.cli.*;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.enums.LanguageType;

/**
 * @since 2/2/2024
 */
@Service
@AllArgsConstructor
public class CommandLineInputParserImpl implements CommandLineInputParser {
    AppProperty appProperty;
    @Override
    public CommandLineInput parse(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(createOptions(), args);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String repositoryCloneDirectory = commandLine.getOptionValue("clone-directory", appProperty.getRepositoryBasePath());
        String repositoryUrl = commandLine.getOptionValue("repository-url");
        String repositoryName = Util.findRepositoryName(repositoryUrl);

        String remoteRepositoryUrl;
        if (!repositoryUrl.startsWith("https://") && !repositoryUrl.startsWith("git@")){
            remoteRepositoryUrl = Util.getRepoUrlFromLocalPath(repositoryUrl);
        }else {
            remoteRepositoryUrl = repositoryUrl;
        }
        String tracerNameText = commandLine.getOptionValue("tracer-name", null);
        String oracleFileIdText = commandLine.getOptionValue("oracle-file-id");
        String endLine = commandLine.getOptionValue("end-line", null);
        return CommandLineInput.builder()
                .command(commandLine.getOptionValue("command"))
                .tracerName(tracerNameText != null ? TracerName.fromCode(tracerNameText) : null)
                .oracleFileId(oracleFileIdText != null ? Integer.parseInt(oracleFileIdText.trim()) : null)
                .cloneDirectory(repositoryCloneDirectory)
                .repositoryUrl(remoteRepositoryUrl)
                .repositoryName(repositoryName)
                .startCommitHash(commandLine.getOptionValue("start-commit", "HEAD"))
                .languageType(LanguageType.valueOf(commandLine.getOptionValue("language", "Java").toUpperCase()))
                .file(commandLine.getOptionValue("file"))
                .methodName(commandLine.getOptionValue("method-name"))
                .startLine(Integer.parseInt(commandLine.getOptionValue("start-line")))
                .endLine(endLine != null ? Integer.parseInt(endLine) : null)
                .outputFile(commandLine.getOptionValue("output-file"))
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
                        .longOpt("tracer-name")
                        .hasArg(true)
                        .desc("Tracer Name")
                        .required(false)
                        .build())
                .addOption(Option.builder()
                        .longOpt("oracle-file-id")
                        .hasArg(true)
                        .desc("Oracle File ID")
                        .required(false)
                        .build())
                .addOption(Option.builder()
                        .longOpt("clone-directory")
                        .hasArg(true)
                        .desc("Full path on the local system where repositories will be stored, default is current directory")
                        .required(false)
                        .build())
                .addOption(Option.builder()
                        .longOpt("repository-url")
                        .hasArg(true)
                        .desc("Repository local path or remote URL")
                        .required(true)
                        .build());


        options.addOption(Option.builder()
                .longOpt("start-commit")
                .hasArg(true)
                .desc(" Start commit hash, default is HEAD")
                .required(false)
                .build());

        options.addOption(Option.builder()
                .longOpt("file")
                .hasArg(true)
                .desc(" Relative path file path from the root of repository")
                .required(true)
                .build());

        options.addOption(Option.builder()
                .longOpt("method-name")
                .hasArg(true)
                .desc(" Method name to trace change history")
                .required(true)
                .build());

        options.addOption(Option.builder()
                .longOpt("start-line")
                .hasArg(true)
                .desc(" Start line number of the method")
                .required(true)
                .build());
        options.addOption(Option.builder()
                .longOpt("end-line")
                .hasArg(true)
                .desc(" End line number of the method")
                .required(false)
                .build());
        options.addOption(Option.builder()
                .longOpt("language")
                .hasArg(true)
                .desc(" Programming Language the method is written in")
                .required(false)
                .build());
        options.addOption(Option.builder()
                .longOpt("output-file")
                .hasArg(true)
                .desc(" Path to write output")
                .required(false)
                .build());
        return options;
    }
}
