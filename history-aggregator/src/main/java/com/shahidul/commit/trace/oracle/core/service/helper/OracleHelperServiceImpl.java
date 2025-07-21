package com.shahidul.commit.trace.oracle.core.service.helper;

import com.shahidul.commit.trace.oracle.core.enums.TracerName;
import com.shahidul.commit.trace.oracle.core.model.InputOracle;
import com.shahidul.commit.trace.oracle.core.mongo.entity.CommitUdt;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import com.shahidul.commit.trace.oracle.util.Util;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import rnd.git.history.finder.enums.LanguageType;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 5/1/2024
 */
@Service
@AllArgsConstructor
public class OracleHelperServiceImpl implements OracleHelperService {
    public static final Map<String, String> repoMap = new HashMap<>();
    private static final MessageDigest DIGESTER;
    static {
        try {
            DIGESTER = MessageDigest.getInstance("SHA-256");
            repoMap.put("checkstyle", "https://github.com/checkstyle/checkstyle.git");
            repoMap.put("commons-lang", "https://github.com/apache/commons-lang.git");
            repoMap.put("flink", "https://github.com/apache/flink.git");
            repoMap.put("hibernate-orm", "https://github.com/hibernate/hibernate-orm.git");
            repoMap.put("javaparser", "https://github.com/javaparser/javaparser.git");
            repoMap.put("jgit", "https://gerrit.googlesource.com/jgit");
            repoMap.put("junit4", "https://github.com/junit-team/junit4.git");
            repoMap.put("junit5", "https://github.com/junit-team/junit5.git");
            repoMap.put("okhttp", "https://github.com/square/okhttp.git");
            repoMap.put("spring-framework", "https://github.com/spring-projects/spring-framework.git");
            repoMap.put("commons-io", "https://github.com/apache/commons-io.git");
            repoMap.put("elasticsearch", "https://github.com/elastic/elasticsearch.git");
            repoMap.put("hadoop", "https://github.com/apache/hadoop.git");
            repoMap.put("hibernate-search", "https://github.com/hibernate/hibernate-search.git");
            repoMap.put("intellij-community", "https://github.com/JetBrains/intellij-community.git");
            repoMap.put("jetty.project", "https://github.com/eclipse/jetty.project.git");
            repoMap.put("lucene-solr", "https://github.com/apache/lucene-solr.git");
            repoMap.put("mockito", "https://github.com/mockito/mockito.git");
            repoMap.put("pmd", "https://github.com/pmd/pmd.git");
            repoMap.put("spring-boot", "https://github.com/spring-projects/spring-boot.git");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public TraceEntity build(InputOracle inputOracle) {
        String uid = generateOracleHash(inputOracle);
        return TraceEntity.builder()
                .uid(uid)
                .oracleFileName(inputOracle.getRepositoryName() + '-' + Util.extractLastPart(inputOracle.getFile()) + "-" + inputOracle.getElement())
                .repositoryName(inputOracle.getRepositoryName())
                .repositoryUrl(inputOracle.getRepositoryUrl())
                .startCommitHash(inputOracle.getStartCommitHash())
                .file(inputOracle.getFile())
                .languageType(LanguageType.from(inputOracle.getLanguage()))
                .elementType(inputOracle.getElementType())
                .elementName(inputOracle.getElement())
                .startLine(inputOracle.getStartLine())
                .endLine(inputOracle.getEndLine())
                .expectedCommits(
                        inputOracle.getCommits().stream().map(commit -> CommitUdt.builder()
                                        .tracerName(TracerName.EXPECTED.getCode())
                                        .commitHash(commit.getCommitHash())
                                        .changeTags(commit.getChangeTags())
                                        .build())
                                .toList()
                )
                .analysis(new HashMap<>())
                .build();
    }

    @Override
    public String generateOracleHash(InputOracle inputOracle) {
        String text = new StringBuilder()
                .append(inputOracle.getRepositoryName())
                .append(inputOracle.getRepositoryUrl())
                .append(inputOracle.getStartCommitHash())
                .append(inputOracle.getFile())
                .append(inputOracle.getElementType())
                .append(inputOracle.getElement())
                .append(inputOracle.getStartLine())
                //.append(inputOracle.getEndLine())
                .toString();
        return DatatypeConverter.printHexBinary(DIGESTER.digest(text.getBytes(StandardCharsets.UTF_8)));
    }
}
