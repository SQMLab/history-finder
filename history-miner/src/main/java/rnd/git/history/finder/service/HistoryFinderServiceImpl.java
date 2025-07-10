package rnd.git.history.finder.service;

import lombok.extern.slf4j.Slf4j;
import rnd.git.history.finder.algortihm.Algorithm;
import rnd.git.history.finder.algortihm.implementation.MethodHistoryAlgorithm;
import rnd.git.history.finder.dto.*;
import rnd.git.history.finder.enums.LanguageType;
import rnd.git.history.finder.jgit.JgitService;
import rnd.git.history.finder.parser.Parser;
import rnd.git.history.finder.parser.implementation.YJavaParser;
import rnd.git.history.finder.util.HistoryFinderOutputConverter;
import rnd.git.history.finder.util.HistoryFinderOutputConverterImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Shahidul Islam
 * @since 2/2/2024
 */
@Slf4j
public class HistoryFinderServiceImpl implements HistoryFinderService {
    HistoryFinderOutputConverter converter = new HistoryFinderOutputConverterImpl();

    @Override
    public HistoryFinderOutput findSync(HistoryFinderInput input) {
        long startTime = System.nanoTime();


        try {
            JgitService jgitService = new JgitService(input.getCloneDirectory(), input.getRepositoryUrl(), input.getRepositoryName());
            Parser parser;
            if (input.getLanguageType() == LanguageType.JAVA) {
                parser = new YJavaParser(jgitService);
            } else {
                throw new RuntimeException("Language " + input.getLanguageType() + " is not supported yet");
            }
            Algorithm algorithm
                    // = new CodeShovelAlgorithm(parser);
                    = new MethodHistoryAlgorithm(parser);

            String startCommitId = null;
            if ("HEAD".equalsIgnoreCase(input.getStartCommitHash())) {
                startCommitId = jgitService.getHeadCommitHash();
            } else {
                startCommitId = input.getStartCommitHash();
            }
            Method method = parser.retrieveGivenMethodFromFile(startCommitId, input.getFile(), input.getMethodName(), input.getStartLine());
            algorithm.compute(method);
            List<HistoryEntry> changeHistory = method.getHistoryEntryList();
            for (HistoryEntry commit : changeHistory) {
                Date commitedAt = null;
               /* if (commit.getCommitInfo() != null){
                    commitedAt = new Date(commit.getCommitInfo().getTime() * 1000L);
                }*/
                log.info("{} -- {}", commit.getNewMethodHolder().getCommitHash(), Arrays.toString(commit.getChangeTagSet().toArray()));
            }
            long endTime = System.nanoTime();
            long totalTime = (endTime - startTime) / 1_000_000;
            log.info("execution time: " + totalTime + " milliseconds");


            CommitTraceOutput traceOutput = converter.convert(jgitService, input, changeHistory, totalTime, method.getAnalyzedCommitCount(), method.getMethodId());
            return HistoryFinderOutput.builder()
                    .commitList(Collections.emptyList())
                    .historyEntryList(changeHistory)
                    .executionTime(totalTime)
                    .analyzedCommitCount(method.getAnalyzedCommitCount())
                    .methodId(method.getMethodId())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
