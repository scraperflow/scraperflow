package scraper.utils;

import org.apache.commons.io.FilenameUtils;
import scraper.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public final class FileUtil {
    private FileUtil(){}



    /** Searches for files with the candidate paths in both file system and class loader resources */
    @NotNull
    public static File getFirstExisting(@NotNull final String name, @NotNull final String... candidatePaths) throws FileNotFoundException {
        return getFirstExisting(name, List.of(candidatePaths));
    }

    /** Searches for files with the candidate paths in both file system and class loader resources */
    @NotNull
    public static File getFirstExisting(@NotNull final String name, @NotNull final Collection<String> candidates) throws FileNotFoundException {
        // base case
        if(new File(name).exists()) return new File(name);

        // try base
        for (String path : candidates) {
            File f = new File(Paths.get(path, name).toString());
            if(f.exists()) return f;
        }

        // try classloader
        for (String path : candidates) {
            URL url = FileUtil.class.getClassLoader().getResource(Paths.get(path, name).toString());
            if(url != null && new File(url.getFile()).exists())
                return new File(url.getFile());
        }

        throw new FileNotFoundException("'"+name+"'"+ " not found among candidate paths: " +candidates);
    }

    /** Searches for files with the candidate paths in both file system and class loader resources */
    @NotNull
    public static File getFirstExistingPaths(@NotNull final String name, @NotNull final Collection<Path> candidates) throws FileNotFoundException {
        return getFirstExisting(name, candidates.stream().map(Path::toString).collect(Collectors.toList()));
    }

    /** Function to apply when a file has changed */
    @FunctionalInterface
    public interface OnFileChange {
        void accept(@NotNull final String filepath);
    }

    /** Monitors given file for changes. If file has changed, executes given function */
    public static void watchFile(@NotNull final String file, @NotNull final OnFileChange actionOnChange) throws IOException, InterruptedException {
        if(new File(file).getParentFile() == null)
            throw new IOException("Watched file '"+ file+"' has to have its own directory!");

        final Path path = FileSystems.getDefault().getPath(new File(file).getParentFile().getPath());
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, ENTRY_MODIFY, ENTRY_DELETE);

            while (true) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    //we only register "ENTRY_MODIFY" so the context is always a Path.
                    final Path changed = (Path) event.context();
                    if (file.endsWith(changed.toString())) {
                        actionOnChange.accept(file);
                    }
                }

                if(!new File(file).exists()) {
                    wk.cancel();
                }

                // reset the key
                boolean valid = wk.reset();
                if (!valid) {
                    break;
                }
            }
        }

        throw new IOException("File does not exist anymore or watch canceled");
    }

    /** Get the parent of a path. Returns default, if there is no parent */
    @NotNull
    public static Path getParentPath(@NotNull final String path, @NotNull final String defaultPath) {
        Path p = Paths.get(path).getParent();
        if(p == null)
            return Paths.get(defaultPath);

        return p;
    }

    /** Replace the extension of a file, if any */
    @NotNull
    public static String replaceFileExtension(@NotNull final Path scrapePath, @NotNull final String extension) {
        // TODO correct?
        return FilenameUtils.removeExtension(scrapePath.toFile().getPath()) + "." +extension;
    }

    /** Replace the extension of a file, if any */
    @NotNull
    public static String replaceFileExtension(@NotNull final String scrapePath, @NotNull final String extension) {
        return FilenameUtils.removeExtension(scrapePath) + "." +extension;
    }

    /**
     * Get all files for a glob pattern and apply an action on them.
     *
     * @param glob glob pattern for file matching
     * @param location location to apply the glob against
     * @param consumer what to execute for each match
     * @throws IOException I/O error by visitor method
     * @see FileSystem#getPathMatcher(String) how to specify the glob
     */
    public static void match(@NotNull final String glob, @NotNull final String location, @NotNull final Consumer<Path> consumer) throws IOException {
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob);

        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (pathMatcher.matches(path)) {
                    consumer.accept(path);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
