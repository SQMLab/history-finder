package rnd.method.history.commit.trace.oracle.cmd.writer;

/**
 * @since 19/5/24
 **/
public interface OutputFileWriter {
    void write(String file, String content);
    void write(String file, Object value);
}
