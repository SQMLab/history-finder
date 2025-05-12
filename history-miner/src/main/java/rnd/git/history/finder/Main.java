package rnd.git.history.finder;

import rnd.git.history.finder.cmd.CommandLineInputParserImpl;
import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.service.HistoryFinderServiceImpl;

public class Main {
    /*
    Required arguments to pass while running program via command line:

    Run Command : java -jar <history finder jar file>
    Arguments:
         -cache-directory <Mandatory : Repository cache directory>
         -repourl <Mandatory : Repository URL>
         -startcommit <Mandatory : Commit hash to begin with tracing commit change history in reverse chronological order>
         -file <Mandatory : Relative path file path from the root of repository>
         -methodname <Mandatory : Method name to trace change history>
         -startline <Mandatory : Start line number of the method>
         -language <Mandatory : Programming Language the method is written in>
         -outfile <Optional : Path to write output>
     */
    public static void main(String[] args) {


        CommandLineInputParserImpl commandLineInputParser = new CommandLineInputParserImpl();
        HistoryFinderInput finderInput = commandLineInputParser.parse(args);

        HistoryFinderServiceImpl historyFinderService = new HistoryFinderServiceImpl();
        historyFinderService.findSync(finderInput);
    }


}
