package scraper.api.service;

import java.io.File;
import java.io.IOException;

/**
 * Provides conflict-free synchronized access to I/O operations on the underlying file system.
 *
 * @since 1.0.0
 */
public interface FileService {
    /** Ensures that a files exists for specified path */
    void ensureFile(String path) throws IOException;

    /** Checks if a file denoted by the path contains a line that starts with given string */
    boolean containsLineStartsWith(String path, String lineStart) throws IOException;

    /** Returns the first line of the file denoted by the given path starting with given string */
    String getFirstLineStartsWith(String path, String lineStart) throws IOException;

    /** Appends given line to file denoted by the given path */
    void appendToFile(String path, String outputLine);

    /** Ensures the directory structure for given file */
    void ensureDirectory(File file) throws IOException;

    /** Replaces a file denoted by given path with string content */
    void replaceFile(String path, String content) throws IOException;

    /** Creates a temporary directory and returns it */
    File getTemporaryDirectory();
}
