package rnd.git.history.finder.parser.implementation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.javaparser.ast.expr.AnnotationExpr;
import lombok.extern.slf4j.Slf4j;
import rnd.git.history.finder.dto.CacheableMap;
import rnd.git.history.finder.Util;
import rnd.git.history.finder.dto.Method;
import rnd.git.history.finder.dto.MethodHolder;
import rnd.git.history.finder.dto.MethodMap;
import rnd.git.history.finder.hash.SimhashGenerator;
import rnd.git.history.finder.jgit.JgitService;
import rnd.git.history.finder.parser.Parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

@Slf4j
public class YJavaParser implements Parser {
    private JgitService jgitService;

    private final Cache<String, MethodMap> methodCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();


    private static final SimhashGenerator simhashGenerator = new SimhashGenerator();
    private static final boolean debug = false;

    public YJavaParser(JgitService jgitService) {
        this.jgitService = jgitService;
    }

    public JgitService getJgitService() {
        return jgitService;
    }

    @Override
    public Method retrieveGivenMethodFromFile(String commitHash, String file, String methodName, Integer startLine) {
        try {
            return constructMethod(findMethod(commitHash, file, methodName, startLine));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean IsTheIdenticalMethodHere(String methodCode, String filePath, String commitName) {
        try {
            String content = jgitService.getFileContent(commitName, filePath);
            CompilationUnit cu = parseCompilationUnit(content);
            for (MethodDeclaration methodDeclaration : cu.findAll(MethodDeclaration.class)) {
                if (methodDeclaration.toString().equals(methodCode)) {
                    return true;
                }

            }
        } catch (IOException exception) {
            return false;
        }
        return false;
    }

    @Override
    public MethodHolder findMethod(String commitHash, String file, String methodName, Integer startLine) {
        //        try {
//            String content = JgitService.getFileContent(Settings.startCommit, Settings.containerFilePath);
//            CompilationUnit cu = com.github.javaparser.JavaParser.parse(content);
//
//            for(MethodDeclaration methodDeclaration: cu.findAll(MethodDeclaration.class)){
//
//                if(methodDeclaration.getName().toString().equals(Settings.methodName)
//                        && methodDeclaration.getName().getBegin().get().line == Settings.methodStartLineNumber){
//                    return constructMethod(methodDeclaration);
//                }
//            }
//
//        }catch(IOException exception){
//        }
//        //System.out.println(content);
//        System.out.println("Invalid method name or line number");z
        try {
            MethodMap allMethodsInFile = getAllMethodsInFile(commitHash, file, true);
            Optional<MethodSourceInfo> intendedMethod = allMethodsInFile.values().stream()
                    .filter(m -> m.getMethodDeclaration().getSignature().getName().equals(methodName))
                    .sorted(Comparator.comparing(m -> Math.abs(m.getStartLine() - startLine))).findFirst();
            if (intendedMethod.isPresent()) {
                return MethodHolder.builder().commitHash(commitHash).file(file).methodSourceInfo(intendedMethod.get()).build();
            } else {
                log.info("Invalid method name or line number");
                throw new RuntimeException("Method not found");
            }
        } catch (Exception e) {
            log.error("Failed to parse", e);
            throw new RuntimeException("Method not found", e);
        }
    }

    @Override
    public MethodHolder findMethod(String commitHash, String file, String methodName, String fullMethodSignature) {
        try {
            Optional<MethodSourceInfo> intendedMethod = getAllMethodsInFile(commitHash, file, true)
                    .getAllByMethodName(methodName)
                    .stream()
                    .filter(m -> m.getMethodDeclaration().getDeclarationAsString().equals(fullMethodSignature))
                    .findFirst();
            if (intendedMethod.isPresent()) {
                return MethodHolder.builder().commitHash(commitHash).file(file).methodSourceInfo(intendedMethod.get()).build();
            } else {
                log.info("Invalid method name or line number");
                throw new RuntimeException("Method not found");
            }
        } catch (Exception e) {
            log.error("Failed to parse", e);
            throw new RuntimeException("Method not found", e);
        }
    }

    //body, full method
    public Map<String, String> getAllMethodBodies(String filePath, String commitName) {
        try {
            Map<String, String> bodies = new LinkedHashMap<>();
            String content = jgitService.getFileContent(commitName, filePath);
            CompilationUnit cu = parseCompilationUnit(content);
            for (MethodDeclaration methodDeclaration : cu.findAll(MethodDeclaration.class)) {
                try {
                    bodies.put(methodDeclaration.getBody().get().toString(), methodDeclaration.toString());
                } catch (NoSuchElementException e) {
                    // some method don't have bodies
                }
            }
            return bodies;
        } catch (IOException exception) {

        }
        return null;
    }

    public Method constructMethod(MethodHolder methodHolder) throws IOException {
        return new Method(methodHolder);
    }

    public String getBodyOfMethod(String methodCode) {
        MethodDeclaration methodDeclaration = getMethodCu(methodCode);
        return methodDeclaration.getBody().get().toString();
    }

    public String getSignatureOfMethod(String methodCode) {
        MethodDeclaration methodDeclaration = getMethodCu(methodCode);
        return methodDeclaration.getDeclarationAsString();
    }

    public MethodDeclaration getMethodCu(String methodCode) {
        return parseMethodDeclaration(methodCode);
    }

    //signature, full method 
    public Map<String, String> getAllMethodSig(String file, String commit) {
        Map<String, String> sigToCode = new HashMap<>();
        try {
            String content = jgitService.getFileContent(commit, file);
            CompilationUnit cu = parseCompilationUnit(content);
            for (MethodDeclaration methodDeclaration : cu.findAll(MethodDeclaration.class)) {
                sigToCode.put(methodDeclaration.getDeclarationAsString().toString(), methodDeclaration.toString());
            }
            return sigToCode;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * @param file,           commitHash
     * @param insertIntoCache
     * @return Map of signature, MethodSourceInfo
     * @throws IOException
     */
    public MethodMap getAllMethodsInFile(String commitHash, String file, Boolean insertIntoCache) {
        byte[] fileContentBytes;
        String fileContentHash;
        try {
            fileContentBytes = jgitService.readFileContentByte(commitHash, file, insertIntoCache);
            fileContentHash = Util.sha1(fileContentBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MethodMap sigToMethodDeclaration = new MethodMap();
        try {
            MethodMap cacheIfPresent = methodCache.getIfPresent(fileContentHash);
            if (cacheIfPresent != null) {
                return cacheIfPresent;
            }

            List<String> lineListInFile = Util.toLineList(fileContentBytes);
            String fileContentText = lineListInFile.stream().map(Object::toString)
                    .collect(Collectors.joining("\n"));
            CompilationUnit cu = parseCompilationUnit(fileContentText);
            for (MethodDeclaration methodDeclaration : cu.findAll(MethodDeclaration.class)) {
                /*int startLine = methodDeclaration.getBegin().get().line;
                int endLine = methodDeclaration.getEnd().get().line;*/
                int startLine = methodDeclaration.getName().getBegin().isPresent() ? methodDeclaration.getName().getBegin().get().line : 0;
                int startLineIncludingDoc = methodDeclaration.getName().getBegin().isPresent() ? methodDeclaration.getName().getBegin().get().line : 0;
                startLineIncludingDoc = Math.min(methodDeclaration.getJavadocComment().isPresent() ? methodDeclaration.getJavadocComment().get().getBegin().get().line : startLineIncludingDoc, startLineIncludingDoc);
                startLineIncludingDoc = Math.min(methodDeclaration.getComment().isPresent() ? methodDeclaration.getComment().get().getBegin().get().line : startLineIncludingDoc, startLineIncludingDoc);
                int endLine = methodDeclaration.getEnd().isPresent() ? methodDeclaration.getEnd().get().line : 0;
                int methodAnnotationSize = methodDeclaration.getAnnotations().size();
                String methodsRawSource = lineListInFile.subList(startLine - 1, endLine).stream().map(Object::toString)
                        .collect(Collectors.joining("\n"));

                String fullCode = lineListInFile.subList(startLineIncludingDoc - 1, endLine).stream().map(Object::toString)
                        .collect(Collectors.joining("\n"));

                String annotationText = methodDeclaration.getAnnotations()
                        .stream()
                        .map(AnnotationExpr::getTokenRange)
                        .map(tokenRange -> tokenRange.isPresent() ? tokenRange.toString() : "").collect(Collectors.joining("\n"));
                MethodSourceInfo msi = MethodSourceInfo.builder()
                        .methodDeclaration(methodDeclaration)
                        .startLine(startLine)
                        .endLine(endLine)
                        .methodRawSourceCode(methodsRawSource)
                        .fullCode(fullCode)
                        .annotation(annotationText)
                        .build();

        /*        long[] simhash = simhashGenerator.generateSimhash(msi);

                msi.setSimHash1(simhash[0]);
                msi.setSimHash2(simhash[1]);

                String methodName = methodDeclaration.getNameAsString();*/
                sigToMethodDeclaration.put(msi);

              /* avoid inner class method with same signature
               if (sigToMethodDeclaration.containsKey(signatureKey)) {
                    Optional<ClassOrInterfaceDeclaration> ancestorType = methodDeclaration.getAncestorOfType(ClassOrInterfaceDeclaration.class);
                    if (ancestorType.isPresent() && file.toLowerCase().endsWith(ancestorType.get().getName().toString().toLowerCase() + ".java")) {
                        sigToMethodDeclaration.put(signatureKey, msi);
                    }
                } else {
                    sigToMethodDeclaration.put(signatureKey, msi);
                }*/

                if (debug) {
                    System.out.println("************************************");
                    System.out.println("LINE : " + msi.getStartLine());
                    System.out.println(methodDeclaration.toString());
                    System.out.println("************************************");
                }
            }
            if (insertIntoCache) {
                methodCache.put(fileContentHash, sigToMethodDeclaration);
            }
            return sigToMethodDeclaration;
        } catch (Exception exception) {
            //sigToMethodDeclaration = null;
            //TODO : How to handle parse error?
            log.info("----------Source Start----------");
            //log.info(fileContentText);
            log.info("----------Source End----------");
            throw new RuntimeException(exception);
        }
    }

    private CompilationUnit parseCompilationUnit(String sourceCode) {
        return new JavaParser()
                .parse(sourceCode).getResult()
                .orElseThrow(() -> new RuntimeException("Failed to to parse code as compilation unit"));
    }

    private MethodDeclaration parseMethodDeclaration(String sourceCode) {
        return new JavaParser()
                .parseBodyDeclaration(sourceCode)
                .getResult()
                .orElseThrow(() -> new RuntimeException("Failed to parse code as method declaration"))
                .asMethodDeclaration();
    }

}
