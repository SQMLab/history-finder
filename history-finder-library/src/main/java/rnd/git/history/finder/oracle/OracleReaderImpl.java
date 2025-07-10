package rnd.git.history.finder.oracle;

import com.fasterxml.jackson.databind.ObjectMapper;
import rnd.git.history.finder.dto.InputOracle;

import java.io.File;
import java.io.IOException;

/**
 * @author Shahidul Islam
 * @since 23/5/24
 **/
public class OracleReaderImpl implements OracleReader {
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public InputOracle readFromOracle(Integer oracleId) {
        try {
            File oracleFile = findFile(oracleId);
            InputOracle inputOracle = objectMapper.readValue(oracleFile, InputOracle.class);
            inputOracle.setOracleFileName(oracleFile.getName());
            return inputOracle;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File findFile(Integer oracleId){
        for (File file : listFile()){
            String oracleIdPrefix = file.getName().split("-")[0];
            if (Integer.parseInt(oracleIdPrefix) == oracleId){
                return file;
            }
        }
        throw new RuntimeException("Oracle File not found : " + oracleId);
    }
    private File[] listFile(){
        return new File(OracleReader.class.getClassLoader().getResource("oracle").getFile())
                .listFiles();
    }
}
