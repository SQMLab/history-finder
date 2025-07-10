package rnd.git.history.finder.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import rnd.git.history.finder.dto.HistoryFinderInput;
import rnd.git.history.finder.enums.LanguageType;
import rnd.git.history.finder.service.HistoryFinderService;
import rnd.git.history.finder.service.HistoryFinderServiceImpl;

/**
 * Unit test for simple App.
 */
public class PreflightTest
        extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PreflightTest(String testName) {
        super(testName);
    }

    HistoryFinderService historyFinderService = new HistoryFinderServiceImpl();

    public void test51JGitRepository() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://gerrit.googlesource.com/jgit")
                .startCommitHash("bd1a82502680b5de5bf86f6c4470185fd1602386")
                .repositoryName("jgit")
                .languageType(LanguageType.JAVA)
                .file("org.eclipse.jgit/src/org/eclipse/jgit/api/CommitCommand.java")
                .methodName("call")
                .startLine(161)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    public void test52JGitRepository() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://gerrit.googlesource.com/jgit")
                .startCommitHash("bd1a82502680b5de5bf86f6c4470185fd1602386")
                .repositoryName("jgit")
                .languageType(LanguageType.JAVA)
                .file("org.eclipse.jgit/src/org/eclipse/jgit/lib/IndexDiff.java")
                .methodName("diff")
                .startLine(409)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    public void testCheckstyleRepository() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://github.com/checkstyle/checkstyle.git")
                .startCommitHash("119fd4fb33bef9f5c66fc950396669af842c21a3")
                .repositoryName("checkstyle")
                .languageType(LanguageType.JAVA)
                .file("src/main/java/com/puppycrawl/tools/checkstyle/Checker.java")
                .methodName("fireErrors")
                .startLine(384)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    public void testCheckstyleRepositoryOracle02() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://github.com/checkstyle/checkstyle.git")
                .startCommitHash("119fd4fb33bef9f5c66fc950396669af842c21a3")
                .repositoryName("checkstyle")
                .languageType(LanguageType.JAVA)
                .file("src/main/java/com/puppycrawl/tools/checkstyle/Checker.java")
                .methodName("process")
                .startLine(206)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    public void testCheckstyleRepositoryOracle03() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://github.com/checkstyle/checkstyle.git")
                .startCommitHash("119fd4fb33bef9f5c66fc950396669af842c21a3")
                .repositoryName("checkstyle")
                .languageType(LanguageType.JAVA)
                .file("src/main/java/com/puppycrawl/tools/checkstyle/utils/CommonUtils.java")
                .methodName("createPattern")
                .startLine(104)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    public void test05() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://github.com/checkstyle/checkstyle.git")
                .startCommitHash("119fd4fb33bef9f5c66fc950396669af842c21a3")
                .repositoryName("checkstyle")
                .languageType(LanguageType.JAVA)
                .file("src/main/java/com/puppycrawl/tools/checkstyle/checks/javadoc/JavadocMethodCheck.java")
                .methodName("checkThrowsTags")
                .startLine(875)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    public void test09() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://github.com/checkstyle/checkstyle.git")
                .startCommitHash("119fd4fb33bef9f5c66fc950396669af842c21a3")
                .repositoryName("checkstyle")
                .languageType(LanguageType.JAVA)
                .file("src/main/java/com/puppycrawl/tools/checkstyle/checks/whitespace/WhitespaceAroundCheck.java")
                .methodName("isNotRelevantSituation")
                .startLine(412)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    public void testJavaParser() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://github.com/javaparser/javaparser.git")
                .startCommitHash("97555053af3025556efe1a168fd7943dac28a2a6")
                .repositoryName("javaparser")
                .languageType(LanguageType.JAVA)
                .file("javaparser-symbol-solver-core/src/main/java/com/github/javaparser/symbolsolver/javaparsermodel/JavaParserFacade.java")
                .methodName("convertToUsage")
                .startLine(519)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    public void testSpringBootRepository() {

        HistoryFinderInput historyFinderInput = HistoryFinderInput.builder()
                .cloneDirectory("~/dev/project/repository")
                .repositoryUrl("https://github.com/spring-projects/spring-boot.git")
                .startCommitHash("5cfe8dbee950dbf3a8de3ece2f6f3363f13d904a")
                .repositoryName("spring-boot")
                .languageType(LanguageType.JAVA)
                .file("spring-boot-project/spring-boot/src/main/java/org/springframework/boot/context/properties/ConfigurationPropertiesBinder.java")
                .methodName("getBindHandler")
                .startLine(115)
                .build();

        historyFinderService.findSync(historyFinderInput);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(PreflightTest.class);
    }

}
