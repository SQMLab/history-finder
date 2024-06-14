package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Shahidul Islam
 * @since 14/6/24
 **/
@Slf4j
@Repository
@AllArgsConstructor
@ConditionalOnProperty(name = "trace.enable-mongodb", havingValue = "FALSE")
public class FileStoreTraceDao implements TraceDao {
    AppProperty appProperty;
    ObjectMapper objectMapper;

    @Override
    public TraceEntity findByOracleId(Integer oracleFileId) {
        return listTraceFiles()
                .filter(file -> Integer.parseInt(file.getName().split("-")[0]) == oracleFileId)
                .map(this::readByFileName)
                .findFirst()
                .orElse(null);
    }

    @Override
    public TraceEntity findByOracleName(String oracleFileName) {
        return readByFileName(new File(appProperty.getTraceCacheDirectory(), oracleFileName));

    }

    @Override
    public List<TraceEntity> findByOracleFileRange(Integer fromFileId, Integer exclusiveToFileId) {
        return listTraceFiles()
                .filter(file -> {
                    int oracleId = Integer.parseInt(file.getName().split("-")[0]);
                    return fromFileId <= oracleId && oracleId < exclusiveToFileId;
                })
                .map(this::readByFileName)
                .toList();
    }

    @Override
    public TraceEntity findByOracleHash(String oracleHash) {
        return null;
    }

    @Override
    public List<TraceEntity> findAll() {
        return listTraceFiles()
                .map(this::readByFileName)
                .toList();
    }

    @NotNull
    private Stream<File> listTraceFiles() {
        return Arrays.stream(Objects.requireNonNull(new File(appProperty.getTraceCacheDirectory())
                .listFiles()));
    }

    @Override
    public void delete(TraceEntity traceEntity) {
        File outputFile = new File(appProperty.getTraceCacheDirectory(), traceEntity.getOracleFileName());
        if (outputFile.exists()) {
            outputFile.delete();
        }

    }

    @Override
    public TraceEntity save(TraceEntity traceEntity) {
        try {
            File outputFile = createFileIfNotExists(traceEntity.getOracleFileName());
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(outputFile, traceEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return traceEntity;
    }

    @Override
    public List<TraceEntity> saveAll(List<TraceEntity> traceEntityList) {
        for (TraceEntity traceEntity : traceEntityList) {
            save(traceEntity);
        }
        return traceEntityList;
    }

    @Override
    public void deleteAll() {
        listTraceFiles()
                .map(file -> file.delete())
                .toList();
    }

    private TraceEntity readByFileName(File oracleFile) {
        try {
            return objectMapper.readValue(oracleFile, TraceEntity.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File createFileIfNotExists(String oracleFileName) throws IOException {
        File outputFile = new File(appProperty.getTraceCacheDirectory(), oracleFileName);
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        return outputFile;
    }

}
