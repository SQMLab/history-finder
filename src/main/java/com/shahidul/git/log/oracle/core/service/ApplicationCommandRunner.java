package com.shahidul.git.log.oracle.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author Shahidul Islam
 * @since 11/11/2023
 */
@Component
public class ApplicationCommandRunner implements CommandLineRunner {
    @Autowired
    DataSetLoader dataSetLoader;
    @Override
    public void run(String... args) throws Exception {
        dataSetLoader.loadDataSet();
    }
}
