package rnd.git.history.finder;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import rnd.git.history.finder.dto.MethodMap;
import rnd.git.history.finder.dto.MethodSimilarity;
import rnd.git.history.finder.hash.detection.SimHashCodeMatcher;
import rnd.git.history.finder.parser.implementation.MethodSourceInfo;

import com.github.javaparser.ast.body.MethodDeclaration;

import info.debatty.java.stringsimilarity.JaroWinkler;

@Slf4j
public class MethodComparator {

    static JaroWinkler jaroWinkler = new JaroWinkler();

    public static boolean isSignatureSame(MethodDeclaration md1, MethodDeclaration md2) {
        assert md1 != null && md2 != null;
        return md1.getSignature().equals(md2.getSignature());
    }

    public static boolean isDeclarationSame(MethodDeclaration md1, MethodDeclaration md2) {
        assert md1 != null && md2 != null;
        return md1.getDeclarationAsString().equals(md2.getDeclarationAsString());
    }

    public static boolean isMethodSame(MethodDeclaration md1, String methodCode) {
        assert md1 != null && methodCode != null;
        return md1.toString().equals(methodCode);
    }

    public static List<MethodSimilarity> findBestMatchingWithJaroWinkler(
            MethodMap allMth, MethodSourceInfo currentMethodSourceInfo) {

        MethodDeclaration currentMethodDeclaration = currentMethodSourceInfo.getMethodDeclaration();
        List<MethodSimilarity> methodSimilarityList = new ArrayList<>();
        for (MethodSourceInfo methodSourceInfo : allMth.values()) {
            String codeX = currentMethodDeclaration.toString();
            String codeY = methodSourceInfo.getMethodDeclaration().toString();
            /*String codeX = currentMethodDeclaration.getBody().isPresent() ? currentMethodDeclaration.getBody().get().toString() : "";
            String codeY = methodSourceInfo.getMethodDeclaration().getBody().isPresent() ? methodSourceInfo.getMethodDeclaration().getBody().get().toString() : "";*/
            String signatureX = currentMethodDeclaration.getSignature().asString();
            String signatureY = methodSourceInfo.getMethodDeclaration().getSignature().asString();
            String javaDocX = Util.extractJavaDoc(currentMethodDeclaration);
            String javaDocY = Util.extractJavaDoc(methodSourceInfo.getMethodDeclaration());
            double javaDocSimilarity = jaroWinkler.similarity(javaDocX == null ? "" : javaDocX, javaDocY == null ? "" : javaDocY);
            double annotationSimilarity = jaroWinkler.similarity(currentMethodSourceInfo.getAnnotation(), methodSourceInfo.getAnnotation());
            double signatureSimilarity = jaroWinkler.similarity(signatureX, signatureY);
            double codeBlockSimilarity = jaroWinkler.similarity(codeX, codeY);
            //double codeBlockSimilarity = new JaroWinklerDistance().apply(codeX, codeY);
            double combinedSimilarity = ( javaDocSimilarity * 0 + annotationSimilarity * 0 + signatureSimilarity * 7 + codeBlockSimilarity * 93) / 100;
            log.debug("Similarity of {} {} {}", signatureY, codeBlockSimilarity, combinedSimilarity);
            methodSimilarityList.add(MethodSimilarity.builder()
                            .methodSourceInfo(methodSourceInfo)
                            .signatureSimilarity(signatureSimilarity)
                            .javaDocSimilarity(javaDocSimilarity)
                            .annotationSimilarity(annotationSimilarity)
                            .codeBlockSimilarity(codeBlockSimilarity)
                            .overallSimilarity(combinedSimilarity)
                    .build());
        }
        return methodSimilarityList;
    }

    public static List<MethodSimilarity> findBestMatchingCandidateByMethodBody2(
            MethodMap allMth, MethodSourceInfo currentMethodSourceInfo) {
        return findBestMatchingCandidateWithScore2(allMth, "", currentMethodSourceInfo);
    }

    private static List<MethodSimilarity> findBestMatchingCandidateWithScore2(
            MethodMap allMth, String startMethodSig, MethodSourceInfo currentMethodSourceInfo) {

        return SimHashCodeMatcher.INSTANCE.findBestMatchingCandidateWithScore(currentMethodSourceInfo, allMth);
    }

    public double getJaroWinklerSimilarity(String s1, String s2) {
        return jaroWinkler.similarity(s1, s2);
    }

    public static Optional<MethodSimilarity> findTop(List<MethodSimilarity> methodSimilarityList){
        return methodSimilarityList.stream()
                .max(Comparator.comparing(MethodSimilarity::getOverallSimilarity));
    }
    public static Optional<MethodSimilarity> findTop(List<MethodSimilarity> methodSimilarityList, String methodName){
        return methodSimilarityList.stream()
                .filter(methodSimilarity -> methodSimilarity.getMethodSourceInfo().getMethodDeclaration().getSignature().getName().equals(methodName))
                .max(Comparator.comparing(MethodSimilarity::getOverallSimilarity));
    }

    public static Map<String, Integer> methodNameCardinalityDiffMap(MethodMap parentFileMap, MethodMap childFileMap){
        Map<String, Integer> countDiffMap = new HashMap<>();
        parentFileMap.values()
                .forEach(methodSourceInfo -> {
                    String methodName = methodSourceInfo.getMethodDeclaration().getSignature().getName();
                    countDiffMap.put(methodName, countDiffMap.getOrDefault(methodName, 0) + 1);
                });

        childFileMap.values()
                .forEach(methodSourceInfo -> {
                    String methodName = methodSourceInfo.getMethodDeclaration().getSignature().getName();
                    countDiffMap.put(methodName, countDiffMap.getOrDefault(methodName, 0) - 1);

                });
        return countDiffMap;
    }
}
