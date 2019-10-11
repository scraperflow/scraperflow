package scraper.api.service.impl;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.springframework.util.FileSystemUtils;
import scraper.annotations.NotNull;
import scraper.api.service.FileService;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FileServiceImpl implements FileService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(FileServiceImpl.class);

    private final ConcurrentMap<String, File> knownFiles = new ConcurrentHashMap<>();

    private final File tempDir = Files.createTempDir();

    public FileServiceImpl() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(!FileSystemUtils.deleteRecursively(tempDir)) {
                log.warn("Could not delete temporary directory: {}", tempDir.getPath());
            }
        }));
    }

    @Override
    public void ensureFile(@NotNull String output) throws IOException {
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
    public boolean containsLineStartsWith(@NotNull String output, @NotNull String lineStart) throws IOException {
        return getFirstLineStartsWith(output, lineStart) != null;
    }

    @NotNull
    @Override
    public String getFirstLineStartsWith(@NotNull String output, @NotNull String lineStart) throws IOException {
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

    @Override
    public void appendToFile(@NotNull String output, @NotNull String outputLine) {
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
    public void ensureDirectory(@NotNull File file) throws IOException {
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
    public void replaceFile(@NotNull String path, @NotNull String body) throws IOException {
        synchronized (knownFiles.get(path)) {
            replaceFileImpl(new File(path), body);
        }
    }

    @NotNull
    @Override
    public File getTemporaryDirectory() {
        return tempDir;
    }

    private void replaceFileImpl(File file, String body) throws IOException {
        FileWriter fooWriter = new FileWriter(file, false); // true to append
        fooWriter.write(body);
        fooWriter.close();
    }
}
