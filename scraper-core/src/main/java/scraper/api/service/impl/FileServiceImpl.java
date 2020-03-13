package scraper.api.service.impl;

import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.service.FileService;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class FileServiceImpl implements FileService {
    private static final @NotNull Logger log = org.slf4j.LoggerFactory.getLogger(FileServiceImpl.class);

    private @NotNull final ConcurrentMap<String, File> knownFiles = new ConcurrentHashMap<>();
//    private @NotNull final File tempDir = Files.createTempDir();

    public FileServiceImpl() {
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            if(!FileSystemUtils.deleteRecursively(tempDir)) {
//                log.warn("Could not delete temporary directory: {}", tempDir.getPath());
//            }
//        }));
    }

    @Override
    public void ensureFile(@NotNull final String output) throws IOException {
        knownFiles.putIfAbsent(output, new File(output));

        // acquire lock, jvm-wide only
        // assume no outside program modifies files
        synchronized (knownFiles.get(output)) {
            File outputFile = knownFiles.get(output);

            if(outputFile.exists() && outputFile.isDirectory())
                throw new IOException("File is a directory "+ outputFile.getPath());
            if(!outputFile.exists()) {
                // create parent dir structure (if any parent can be found)
                // ignore result and check afterwards for existence
                if(outputFile.getParentFile() != null)
                    //noinspection ResultOfMethodCallIgnored meaningless, throws IOException if structure is not correct
                    outputFile.getParentFile().mkdirs();
                new FileOutputStream(outputFile).close();
                if(!outputFile.exists()) {
                    throw new IOException("Could not create file at "+ outputFile.getPath());
                }
            }
        }
    }

    @Override
    public boolean containsLineStartsWith(@NotNull final String output, @NotNull final String lineStart) throws IOException {
        return getFirstLineStartsWith(output, lineStart) != null;
    }

    @Override
    public @Nullable String getFirstLineStartsWith(@NotNull final String output, @NotNull final String lineStart) throws IOException {
        synchronized (knownFiles.get(output)) {
            File file = knownFiles.get(output);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(lineStart)) {
                        return line;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("File not found even though file is known! Check file system: "+e.getMessage());
            }
        }

        // line not found
        return null;
    }

    @Nullable
    @Override
    public String getFirstLineEquals(@NotNull String output, @NotNull String lineEquals) throws IOException {
        synchronized (knownFiles.get(output)) {
            File file = knownFiles.get(output);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(lineEquals)) {
                        return line;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("File not found even though file is known! Check file system: "+e.getMessage());
            }
        }

        // line not found
        return null;
    }

    @Override
    public boolean ifNoLineEqualsFoundAppend(@NotNull String output, @NotNull String equals, Supplier<String> content) throws IOException {
        synchronized (knownFiles.get(output)) {
            String firstLine = getFirstLineEquals(output, equals);
            if(firstLine == null)
                appendToFile(output, content.get());

            return firstLine != null;
        }
    }

    @Override
    public boolean ifNoLineStartsWithFoundAppend(@NotNull final String output, @NotNull final String lineStart, Supplier<String> content) throws IOException {
        synchronized (knownFiles.get(output)) {
            String firstLine = getFirstLineStartsWith(output, lineStart);
            if(firstLine == null)
                appendToFile(output, content.get());

            return firstLine != null;
        }
    }

    @Override
    public void appendToFile(@NotNull final String output, @NotNull final String outputLine) {
        synchronized (knownFiles.get(output)) {
            File file = knownFiles.get(output);

            try(PrintWriter writer = new PrintWriter(new FileOutputStream(file, true))) {
                writer.println(outputLine);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("File not found even though file is known! Check file system: "+e.getMessage());
            }
        }
    }


    @Override
    public void ensureDirectory(@NotNull final File file) throws IOException {
        if(file.getParentFile() == null) return;

        if (!file.getParentFile().exists()) {
            if(file.getParentFile().exists() && !file.getParentFile().isDirectory())
                throw new IOException("Failed to create parent directory for file, already exists but not directory: "+file);
            if(!file.getParentFile().mkdirs())
                if(!(file.getParentFile().exists() && file.getParentFile().isDirectory()))
                    throw new IOException("Failed to create parent directory for file: "+file);
        }
    }

    @Override
    public void replaceFile(@NotNull final String path, @NotNull final String body) throws IOException {
        synchronized (knownFiles.get(path)) {
            replaceFileImpl(new File(path), body);
        }
    }

    @NotNull
    @Override
    public File getTemporaryDirectory() {
        throw new IllegalStateException("Temporary dirs not implemented");
//        return tempDir;
    }

    private void replaceFileImpl(@NotNull final File file, @NotNull final String body) throws IOException {
        FileWriter fooWriter = new FileWriter(file, false); // true to append
        fooWriter.write(body);
        fooWriter.close();
    }
}
