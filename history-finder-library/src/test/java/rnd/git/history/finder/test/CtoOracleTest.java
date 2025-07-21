package rnd.git.history.finder.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import rnd.git.history.finder.Util;
import rnd.git.history.finder.dto.*;
import rnd.git.history.finder.oracle.OracleMapper;
import rnd.git.history.finder.oracle.OracleMapperImpl;
import rnd.git.history.finder.oracle.OracleReader;
import rnd.git.history.finder.oracle.OracleReaderImpl;
import rnd.git.history.finder.service.HistoryFinderService;
import rnd.git.history.finder.service.HistoryFinderServiceImpl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @since 23/5/24
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class CtoOracleTest {

    OracleReader oracleReader = new OracleReaderImpl();
    OracleMapper oracleMapper = new OracleMapperImpl();
    HistoryFinderService historyFinderService = new HistoryFinderServiceImpl();

    @Order(1)
    @TestFactory
    Stream<DynamicNode> executeTest() {

        List<Integer> oracleFileIds = Util.parseOracleFileIds(System.getenv("ORACLE_FILE_IDS"));
        String cacheDirectory = System.getenv("CLONE_DIRECTORY");
        return oracleFileIds.stream()
                .map(oracleFileId -> {
                    InputOracle inputOracle = oracleReader.readFromOracle(oracleFileId);
                    return OracleExecutionContext.builder()
                            .inputOracle(inputOracle)
                            .historyFinderInput(oracleMapper.toHistoryFinderInput(inputOracle, cacheDirectory))
                            .build();
                })
                .map(oracleExecutionContext -> DynamicContainer.dynamicContainer(oracleExecutionContext.getInputOracle().getOracleFileName(),
                        Stream.of(
                                DynamicTest.dynamicTest("History Finder", () -> {
                                    CommitTraceOutput historyFinderOutput = historyFinderService.findSync(oracleExecutionContext.getHistoryFinderInput());
                                    oracleExecutionContext.setOutputCommitSet(historyFinderOutput.getCommitDetails()
                                            .stream().map(OutputCommitDetail::getCommitHash).collect(Collectors.toUnmodifiableSet()));
                                }),
                                DynamicTest.dynamicTest("Commit Count", () -> {
                                    int outputCommitCount = oracleExecutionContext.getOutputCommitSet().size();
                                    int expectedCommitCount = oracleExecutionContext.getInputOracle().getCommits().size();
                                    Assertions.assertEquals(expectedCommitCount, outputCommitCount, () -> "Expected " + expectedCommitCount + " but found " + outputCommitCount);
                                })
                                ,
                                DynamicContainer.dynamicContainer("Expected", oracleExecutionContext.getInputOracle()
                                        .getCommits()
                                        .stream()
                                        .map(inputCommit -> DynamicTest.dynamicTest(inputCommit.getCommitHash(), () -> Assertions.assertTrue(oracleExecutionContext.getOutputCommitSet().contains(inputCommit.getCommitHash()), inputCommit.getCommitHash()))
                                        ))
                                ,
                                DynamicTest.dynamicTest("output", ()-> {
                                    Set<String> expectedCommitSet = oracleExecutionContext.getInputOracle().getCommits().stream().map(InputCommit::getCommitHash).collect(Collectors.toSet());
                                    AtomicInteger unMatchedCount = new AtomicInteger();
                                    String displayText = oracleExecutionContext.getOutputCommitSet().stream().map(commitHash -> {
                                        if (expectedCommitSet.contains(commitHash)) {
                                            return commitHash + " - " + commitHash;

                                        } else {
                                            unMatchedCount.addAndGet(1);
                                            return commitHash + " - ";
                                        }


                                    }).collect(Collectors.joining("\n"));
                                    Assertions.assertEquals(0, unMatchedCount.get(), "\n" + displayText);
                                })
                        ).sequential())
                );
    }
}
