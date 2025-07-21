package rnd.method.history.commit.trace.oracle.cmd.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @since 19/5/24
 **/
@Service
@AllArgsConstructor
@Slf4j
public class OutputFileWriterImpl implements OutputFileWriter {
    ObjectMapper objectMapper;

    @Override
    public void write(String file, String content) {
        try {
            FileWriter fileWriter = new FileWriter(createFileIfNotExists(new File(file)));
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

            File targetFile = new File(file);
            if (targetFile.getParentFile() != null){
                targetFile.getParentFile().mkdirs();
            }
            if (!targetFile.exists()){
                if(targetFile.createNewFile()){
                    log.info("Target file {} created", file);
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File createFileIfNotExists(File file) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }
}
