package com.shahidul.commit.trace.oracle.core.ui;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.ui.dto.MethodLocationDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class GitRepositoryUiServiceImpl implements GitRepositoryUiService {
    AppProperty appProperty;

    @Override
    public List<String> findRepositoryList() {
        File cloneDirectory = new File(appProperty.getRepositoryBasePath());
        String[] repoArrays = cloneDirectory.list();
        List<String> repositoryList = Arrays.asList(repoArrays != null ? repoArrays : new String[0]);
        repositoryList.sort(String.CASE_INSENSITIVE_ORDER);
        return repositoryList;

    }

    @Override
    public List<String> findPathList(String repositoryName, String commitHash, String path) {
        //TODO : checkout commit
        StringBuilder pathBuilder = new StringBuilder(appProperty.getRepositoryBasePath())
                .append("/").append(repositoryName);
        if (!path.isEmpty()) {
            pathBuilder.append("/").append(path);
        }
        File file = new File(pathBuilder.toString());
        if (file.exists()) {
            if (file.isFile()) {
                return List.of(file.getName());
            } else {
                return Arrays.stream(Objects.requireNonNull(file.listFiles(f -> {
                    if (f.isDirectory()) {
                        return !f.getName().startsWith(".");
                    } else {
                        return f.getName().endsWith(".java");
                    }
                }))).map(f -> {
                   /* if (f.isDirectory()) {
                        return f.getName() + "/";
                    }else{
                        return f.getName();
                    }*/
                    return path.isEmpty() ? f.getName() : path + "/" + f.getName();
                }).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<MethodLocationDto> findMethodLocationList(String repositoryName, String commitHash, String file) {
        List<MethodDeclaration> methodDeclarationList = parseAllMethods(repositoryName, commitHash, file);
        return methodDeclarationList.stream()
                .map(dec -> MethodLocationDto.builder()
                        .methodName(dec.getSignature().getName())
                        .signature(dec.getDeclarationAsString())
                        .startLine(dec.getBegin().get().line)
                        .endLine(dec.getEnd().get().line)
                        .build())
                .sorted(Comparator.comparing(MethodLocationDto::getMethodName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }


    private List<MethodDeclaration> parseAllMethods(String repositoryName, String commitHash, String file) {
        File javaFile = new File(appProperty.getRepositoryBasePath() + "/" + repositoryName + "/" + file);
        try {
            String fileContent = FileUtils.readFileToString(javaFile, StandardCharsets.UTF_8);
            CompilationUnit compilationUnit = parseCompilationUnit(fileContent);
            return compilationUnit.findAll(MethodDeclaration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CompilationUnit parseCompilationUnit(String sourceCode) {
        return new JavaParser()
                .parse(sourceCode).getResult()
                .orElseThrow(() -> new RuntimeException("Failed to to parse code as compilation unit"));
    }
}
