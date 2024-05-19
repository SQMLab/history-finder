package com.shahidul.commit.trace.oracle.cmd.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
@Service
@AllArgsConstructor
public class OutputFileWriterImpl implements OutputFileWriter {
    ObjectMapper objectMapper;

    @Override
    public void write(String file, String content) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(String file, Object value) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(file), value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
