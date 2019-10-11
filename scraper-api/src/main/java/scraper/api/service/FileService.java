package scraper.api.service;

import scraper.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Provides conflict-free synchronized access to I/O operations on the underlying file system.
 *
 * @since 1.0.0
 */
public interface FileService {
    /** Ensures that a files exists for specified path */
    void ensureFile(@NotNull String path) throws IOException;

    /** Checks if a file denoted by the path contains a line that starts with given string */
    boolean containsLineStartsWith(@NotNull String path, @NotNull String lineStart) throws IOException;

    /** Returns the first line of the file denoted by the given path starting with given string */
    @NotNull String getFirstLineStartsWith(@NotNull String path, @NotNull String lineStart) throws IOException;

    /** Appends given line to file denoted by the given path */
    void appendToFile(@NotNull String path, @NotNull String outputLine);

    /** Ensures the directory structure for given file */
    void ensureDirectory(@NotNull File file) throws IOException;

    /** Replaces a file denoted by given path with string content */
    void replaceFile(@NotNull String path, @NotNull String content) throws IOException;

    /** Creates a temporary directory and returns it */
    @NotNull File getTemporaryDirectory();
}
