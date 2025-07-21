package rnd.git.history.finder.cmd;

import org.apache.commons.cli.*;
import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.enums.LanguageType;

/**
 * @since 2/2/2024
 */
public class CommandLineInputParserImpl implements CommandLineInputParser {
    @Override
    public HistoryFinderInput parse(String[] args) {
        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(createOptions(), args);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String repositoryCacheDirectory = commandLine.getOptionValue("clone-directory");
        String repositoryUrl = commandLine.getOptionValue("repository-url");
        String[] urlParts = repositoryUrl.replace(".git", "").split("/");
        int repositoryNameIndex =  urlParts.length - 1;
        return HistoryFinderInput.builder()
                .cloneDirectory(repositoryCacheDirectory)
                .repositoryUrl(repositoryUrl)
                .repositoryName(urlParts[repositoryNameIndex])
                .startCommitHash(commandLine.getOptionValue("start-commit", "HEAD"))
                .languageType(LanguageType.valueOf(commandLine.getOptionValue("language", "Java").toUpperCase()))
                .file(commandLine.getOptionValue("file"))
                .methodName(commandLine.getOptionValue("method-name"))
                .startLine(Integer.parseInt(commandLine.getOptionValue("start-line")))
                .outputFile(commandLine.getOptionValue("output-file"))
                .build();
    }

    private Options createOptions() {
        Options options = new Options();

        options.addOption(Option.builder()
                .longOpt("clone-directory")
                .hasArg(true)
                .desc("Full path on the local system where repositories will be stored")
                .required(true)
                .build());
        options.addOption(Option.builder()
                .longOpt("repository-url")
                .hasArg(true)
                .desc("Repository URL e.g. github URL")
                .required(true)
                .build());


        options.addOption(Option.builder()
                .longOpt("start-commit")
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
