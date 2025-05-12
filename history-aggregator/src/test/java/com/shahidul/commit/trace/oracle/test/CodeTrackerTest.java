package com.shahidul.commit.trace.oracle.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codetracker.api.CodeTracker;
import org.codetracker.api.History;
import org.codetracker.api.MethodTracker;
import org.codetracker.change.Change;
import org.codetracker.element.Method;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class CodeTrackerTest {
    @Test
    @SneakyThrows
    public void codeTrackerMethodMergeTest(){
        GitService gitService = new GitServiceImpl();
        try (Repository repository = gitService.cloneIfNotExists("/home/cs/grad/islams32/dev/project/academic/commit-log-chain", "https://github.com")){

            MethodTracker methodTracker = CodeTracker.methodTracker()
                    .repository(repository)
                    .filePath("src/main/java/commit/log/chain/SortingAlgorithm.java")
                    .startCommitId("d200081310cc4c140bc4a034ebf35d50ff04b5c2")
                    .methodName("sort")
                    .methodDeclarationLineNumber(5)

/*                    .startCommitId("ffd13ebb3cb3118f6cc381ea498ec0549ecccffa")
                    .methodName("sortX")
                    .methodDeclarationLineNumber(4)*/
  /*                  .startCommitId("ffd13ebb3cb3118f6cc381ea498ec0549ecccffa")
                    .methodName("sortY")
                    .methodDeclarationLineNumber(14)*/
                    .build();

            History<Method> methodHistory = methodTracker.track();

            for (History.HistoryInfo<Method> historyInfo : methodHistory.getHistoryInfoList()) {
                System.out.println("======================================================");
                System.out.println("Commit ID: " + historyInfo.getCommitId());
                System.out.println("Date: " +
                        LocalDateTime.ofEpochSecond(historyInfo.getCommitTime(), 0, ZoneOffset.UTC));
                System.out.println("Before: " + historyInfo.getElementBefore().getName());
                System.out.println("After: " + historyInfo.getElementAfter().getName());

                for (Change change : historyInfo.getChangeList()) {
                    System.out.println(change.getType().getTitle() + ": " + change);
                }
            }
            System.out.println("======================================================");
        }

    }
}
