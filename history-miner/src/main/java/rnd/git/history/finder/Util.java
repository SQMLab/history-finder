package rnd.git.history.finder;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ReferenceType;
import jakarta.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.revwalk.RevCommit;
import rnd.git.history.finder.dto.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Util {
    public static String toChangeText(Set<ChangeTag> changeTags) {
        return changeTags.stream()
                .map(ChangeTag::getCode)
                .collect(Collectors.joining("/"));
    }

    public static String extractJavaDoc(MethodDeclaration methodDeclaration) {
        if (methodDeclaration != null && methodDeclaration.getJavadocComment().isPresent()) {
            return methodDeclaration.getJavadocComment().orElse(new JavadocComment()).getContent();
        }
        return null;
    }

    public static String extractFileName(String file) {
        if (file != null) {
            String[] pathSegments = file.split("/");
            if (pathSegments.length > 0) {
                return pathSegments[pathSegments.length - 1];
            }
        }
        return null;
    }

    @SneakyThrows
    public static String sha1(byte[] content) {
        MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(content);
        return DatatypeConverter.printHexBinary(crypt.digest());
    }

    public static List<String> toLineList(byte[] bytes) throws IOException {
        return IOUtils.readLines(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
    }
    public static List<Integer> parseOracleFileIds(String fileIdsText) {
        Set<Integer> idSet = new HashSet<>();
        String[] idOrRanges = fileIdsText.replace(" ", "").split(",");
        for (int i = 0, idOrRangesLength = idOrRanges.length; i < idOrRangesLength; i++) {
            String idOrRange = idOrRanges[i];
            if (idOrRange.contains("-")) {
                String[] range = idOrRange.split("-");
                int[] ids = IntStream.rangeClosed(Integer.parseInt(range[0]), Integer.parseInt(range[1]))
                        .toArray();
                for (int id : ids) {
                    idSet.add(id);
                }
            } else {
                idSet.add(Integer.parseInt(idOrRange));
            }
        }
        return idSet.stream()
                .sorted()
                .toList();
    }

    public static double getMethodNameSimilarity(MethodDeclaration parent, MethodDeclaration child){
        if(parent.getSignature().getName().equalsIgnoreCase(child.getSignature().getName())){
            return 1.0;
        }else{
            return 0;
        }
    }

    public static List<String> toLineList(String text) {
        if (text != null){
            return Arrays.asList(text.split("\r\n|\r|\n"));
        }else{
            return new ArrayList<>();
        }
    }
    public static String getPrettyDeclarationAndBody(MethodDeclaration methodDeclaration){
        return methodDeclaration.getDeclarationAsString() + (methodDeclaration.getBody().isPresent() ? methodDeclaration.getBody().get().toString() : "");
    }
    public static boolean isBodyEqual(MethodDeclaration m1, MethodDeclaration m2){
        String m1Body = m1.getBody().isPresent() ? m1.getBody().get().toString() : "";
        String m2Body = m2.getBody().isPresent() ? m2.getBody().get().toString() : "";
        return m1Body.equals(m2Body);
    }
    public static CommitNode toCommitNode(RevCommit revCommit){
        return CommitNode.builder()
                .commitHash(revCommit.getName())
                .ancestorCommitHashes(Arrays.stream(revCommit.getParents()).sequential().map(RevCommit::getName).toList())
                .build();
    }
    public static Yexceptions getInitialExceptions(MethodDeclaration method) {
        List<String> exceptions = new ArrayList<>();
        for (ReferenceType type : method.getThrownExceptions()) {
            exceptions.add(type.asString());
        }
        return new Yexceptions(exceptions);
    }


    /**
     * Copied from CodeShovel
     */
    public static boolean parametersMetadataEqual(List<Yparameter> parametersA, List<Yparameter> parametersB) {
        Map<String, Yparameter> parameterMapA = new HashMap<>();
        for (Yparameter parameter : parametersA) {
            parameterMapA.put(parameter.getNameTypeString(), parameter);
        }

        for (Yparameter paramB : parametersB) {
            String paramString = paramB.getNameTypeString();
            Yparameter paramA = parameterMapA.get(paramString);
            if (paramA != null && !paramA.getMetadataString().equals(paramB.getMetadataString())) {
                return false;
            }
        }

        return true;
    }
    /**
     * Copied from CodeShovel
     */
    public static List<Yparameter> getInitialParameters(MethodDeclaration method) {
        List<Yparameter> parametersList = new ArrayList<>();
        List<Parameter> parameterElements = method.getParameters();
        for (Parameter parameterElement : parameterElements) {
            Yparameter parameter = new Yparameter(parameterElement.getNameAsString(), parameterElement.getTypeAsString());
            Map<String, String> metadata = createParameterMetadataMap(parameterElement);
            parameter.setMetadata(metadata);
            parametersList.add(parameter);
        }
        return parametersList;
    }
    /**
     * Copied from CodeShovel
     */
    public static Ymodifiers getInitialModifiers(MethodDeclaration method) {
        List<String> modifiers = new ArrayList<>();
        for (Modifier modifier : method.getModifiers()) {
            modifiers.add(modifier.toString());
        }
        return new Ymodifiers(modifiers);
    }
    public static boolean isDocumentationChange(MethodDeclaration m1, MethodDeclaration m2){
        String commentX = m1.getComment().isPresent() ? m1.getComment().get().asString() : "";
        String commentY = m2.getComment().isPresent() ? m2.getComment().get().asString() : "";
        if (!commentX.equals(commentY)) {
            return true;
        }
/*
        For unknown weird reason m2.getAllContainedComments() is not returning all the comments(for method history 261)
        List<Comment> xComments = m1.getAllContainedComments();
        List<Comment> yComments = m2.getAllContainedComments();
        if (xComments.size() != yComments.size()) {
            return true;
        }
        Comparator<Comment> commentComparator = (o1, o2) -> o1.getBegin().get().line - o2.getBegin().get().line;
        xComments.sort(commentComparator);
        yComments.sort(commentComparator);
        for (int i = 0; i < xComments.size(); i++) {
            Comment xComment = xComments.get(i);
            Comment yComment = yComments.get(i);
            if (!xComment.asString().equals(yComment.asString())) {
                return true;
            }
        }*/
        return false;
    }
    /**
     * Copied from CodeShovel
     */
    private static Map<String,String> createParameterMetadataMap(Parameter parameterElement) {
        Map<String, String> metadata = new HashMap<>();
        String modifiersString = createParameterModifiersString(parameterElement);
        if (modifiersString != null) {
            metadata.put("modifiers", modifiersString);
        }
        String annotationsString = createParameterAnnotationsString(parameterElement);
        if (annotationsString != null) {
            metadata.put("annotations", annotationsString);
        }
        return metadata;
    }
    /**
     * Copied from CodeShovel
     */
    private static  String createParameterAnnotationsString(Parameter parameterElement) {
        String ret = null;
        List<String> annotations = new ArrayList<String>();
        for (Node node : parameterElement.getAnnotations()) {
            annotations.add(node.toString());
        }
        if (annotations.size() > 0) {
            ret = StringUtils.join(annotations, "-");
        }
        return ret;
    }
    /**
     * Copied from CodeShovel
     */
    private static  String createParameterModifiersString(Parameter parameterElement) {
        String ret = null;
        List<String> modifiers = new ArrayList<String>();
        for (Modifier modifier : parameterElement.getModifiers()) {
            modifiers.add(modifier.toString());
        }
        if (modifiers.size() > 0) {
            ret = StringUtils.join(modifiers, "-");
        }
        return ret;
    }
    /**
     * Copied from CodeShovel
     */
    public static String getInitialId(MethodDeclaration rawMethod) {
        String ident = rawMethod.getSignature().getName();
        String idParameterString = getIdParameterString(rawMethod);
        if (StringUtils.isNotBlank(idParameterString)) {
            ident += "___" + idParameterString;
        }
        return sanitizeFunctionId(ident);
    }
    public static  String getIdParameterString(MethodDeclaration rawMethod) {
        List<String> parts = new ArrayList<>();
        for (Yparameter parameter : getInitialParameters(rawMethod)) {
            parts.add(parameter.toString());
        }
        return StringUtils.join(parts, "__");
    }
    public static String sanitizeFunctionId(String ident) {
        return ident.replaceAll(":", "__").replaceAll("#", "__").replaceAll("<", "__").replaceAll(">", "__");
    }
    public static String extractLastPart(String file){
        String[] fileParts = file.split("/");
        return fileParts[fileParts.length - 1];
    }

    public static Double daysBetweenCommit(int commitTimInSecond, int ancestorCommitTimeInSecond) {
        int commitTimeDiffInSecond = commitTimInSecond -ancestorCommitTimeInSecond;
        double daysBetweenCommits = (double) commitTimeDiffInSecond / (60 * 60 * 24);
        return new BigDecimal(daysBetweenCommits).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @SneakyThrows
    public static String sha256(byte[] content) {
        MessageDigest crypt = MessageDigest.getInstance("SHA-256");
        crypt.reset();
        crypt.update(content);
        return DatatypeConverter.printHexBinary(crypt.digest());
    }
    //TODO: Check it it supports line number or not
    public static String getDiffUrl(String repositoryUrl, String parentCommitHash, String commitHash, String file) {
        String fileSection = "";
        if (file != null){
            fileSection =  "#diff-" + sha256(file.getBytes(StandardCharsets.UTF_8)).toLowerCase();
        }
        return repositoryUrl.replaceAll("\\.git", "") + "/compare/" + parentCommitHash + "..." + commitHash  + fileSection;
    }

    public static String gitRawFileUrl(String repositoryUrl, String commitHash, String file, Integer lineNumber){
        return repositoryUrl.replaceAll("\\.git", "") + "/blob/" + commitHash + "/" + file + (lineNumber != null ? "#L" + lineNumber : "");
    }
    public static String getCommitUrl(String repositoryUrl, String commitHash){
        return repositoryUrl.replaceAll("\\.git", "") + "/commit/" + commitHash;
    }
    public static String getUserSearchUrl(String authorName){
        return "https://github.com/search?q=" + authorName + "&type=Users";
    }

    public static String getDiff(String oldText, String newText) {
    /*    if (oldText == null){
            return newText;
        }else if (newText ==  null){
            return oldText;
        }*/
        try {
            RawText sourceOld = new RawText((oldText == null ? "" : oldText).getBytes(StandardCharsets.UTF_8));
            RawText sourceNew = new RawText((newText == null ? "" : newText).getBytes(StandardCharsets.UTF_8));
            DiffAlgorithm diffAlgorithm = new HistogramDiff();
            RawTextComparator textComparator = RawTextComparator.DEFAULT;
            EditList editList = diffAlgorithm.diff(textComparator, sourceOld, sourceNew);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter formatter = new DiffFormatter(out);
            formatter.setContext(1000);
            formatter.format(editList, sourceOld, sourceNew);
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
    /**
     * Expands paths containing ~ (home), . (current), or .. (parent) into a fully resolved absolute path.
     *
     * @param inputPath The raw input path (e.g., "~/../my-folder/./file.txt")
     * @return The fully expanded and normalized absolute path
     */
    public static String expandPath(String inputPath) {
        if (inputPath == null || inputPath.isEmpty()) {
            throw new IllegalArgumentException("Input path must not be null or empty");
        }

        // Expand ~ to home directory
        if (inputPath.startsWith("~")) {
            String userHome = System.getProperty("user.home");
            inputPath = userHome + inputPath.substring(1);
        }

        try {
            // Convert to Path, resolve absolute path and normalize . and ..
            Path path = Paths.get(inputPath);
            return path.toAbsolutePath().normalize().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to expand path: " + inputPath, e);
        }
    }
}
