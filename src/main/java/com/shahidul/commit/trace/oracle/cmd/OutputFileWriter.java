package com.shahidul.commit.trace.oracle.cmd;

/**
 * @author Shahidul Islam
 * @since 19/5/24
 **/
public interface OutputFileWriter {
    void write(String file, String content);
    void write(String file, Object value);
}
