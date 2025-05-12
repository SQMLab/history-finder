package com.shahidul.commit.trace.oracle.core.mongo.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shahidul.commit.trace.oracle.config.AppProperty;
import com.shahidul.commit.trace.oracle.core.mongo.entity.TraceEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
    public List<TraceEntity> findAllByOracleId(Integer oracleFileId) {
        return listTraceFiles()
                .filter(file -> Integer.parseInt(file.getName().split("-")[0]) == oracleFileId)
                .map(this::readByFileName)
                .toList();
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
    public List<TraceEntity> findByOracleFileIdList(List<Integer> oracleFileIdList) {
        List<TraceEntity> traceEntityList = new ArrayList<>();
        oracleFileIdList.forEach(oracleFileId -> traceEntityList.add(findByOracleId(oracleFileId)));
        return traceEntityList;
    }

    @Override
    public TraceEntity findByOracleHash(String oracleHash) {
        return findAll()
                .stream()
                .filter(traceEntity -> oracleHash.equalsIgnoreCase(traceEntity.getUid()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<TraceEntity> findAll() {
        return listTraceFiles()
                .map(this::readByFileName)
                .toList();
    }

    @NotNull
    private Stream<File> listTraceFiles() {
        File[] listedFiles = new File(appProperty.getTraceCacheDirectory()).listFiles();
        return Arrays.stream(listedFiles == null ? new File[0] : listedFiles);
    }

    @Override
    public void delete(TraceEntity traceEntity) {
        File outputFile = new File(appProperty.getTraceCacheDirectory(), traceEntity.getOracleFileName());
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    @Override
    public void delete(List<TraceEntity> traceEntityList) {
        traceEntityList.forEach(this::delete);
    }

    @Override
    public TraceEntity save(TraceEntity traceEntity) {
        try {
            addDateTimeAndVersion(traceEntity);
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

    private void addDateTimeAndVersion(TraceEntity traceEntity) {
        if (traceEntity.getCreatedAt() == null) {
            traceEntity.setCreatedAt(new Date());
        }
        traceEntity.setUpdatedAt(new Date());
        if (traceEntity.getVersion() == null) {
            traceEntity.setVersion(0);
        }
        traceEntity.setVersion(traceEntity.getVersion() + 1);
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
