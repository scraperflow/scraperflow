package scraper.utils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public final class FileUtil {

    public static File getFirstExisting(String name, String... candidatePaths) throws FileNotFoundException {
        return getFirstExisting(name, Collections.emptyList(), candidatePaths);
    }

    public static File getFirstExisting(String name, Collection<String> base, String... otherBaseCandidates) throws FileNotFoundException {
        // base case
        if(new File(name).exists()) return new File(name);

        base = new ArrayList<>(base);
        base.addAll(Arrays.asList(otherBaseCandidates));

        // try base
        for (String path : base) {
            File f = new File(Paths.get(path, name).toString());
            if(f.exists()) return f;
        }

        // try classloader
        for (String path : base) {
            URL url = FileUtil.class.getClassLoader().getResource(Paths.get(path, name).toString());
            if(url != null && new File(url.getFile()).exists()) return new File(url.getFile());
        }

        throw new FileNotFoundException("'"+name+"'"+ " not found among candidate paths: " +base);
    }

    @FunctionalInterface
    public interface OnFileChange {
        void accept(String filepath);
    }

    /** Monitors given file for changes. If file has changed, executes given function */
    public static void watchFile(String file, OnFileChange actionOnChange) throws IOException, InterruptedException {
        if(new File(file).getParentFile() == null) throw new IOException("Watched file '"+ file+"' has to have its own directory!");

        final Path path = FileSystems.getDefault().getPath(new File(file).getParentFile().getPath());
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (!Thread.currentThread().isInterrupted()) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    //we only register "ENTRY_MODIFY" so the context is always a Path.
                    final Path changed = (Path) event.context();
                    if (file.endsWith(changed.toString())) {
                        actionOnChange.accept(file);
                    }
                }

                // reset the key
                boolean valid = wk.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }


//    private void pollFolder(String folder, Consumer<File> scrapeFile, boolean rootPoller, String jobPojo) {
//        l.info("[{}] Polling folder {}", jobPojo, folder);
//
//        try{
//            do {
//                List<Callable<Object>> polls = new ArrayList<>();
//
//                // current content
//                File presentFiles = new File(folder);
//                for (File file : presentFiles.listFiles()) {
//                    if(file.isDirectory()) {
//                        Runnable watch = () -> pollFolder(file.getPath(), scrapeFile, false, jobPojo);
//                        if(rootPoller) {
//                            polls.add(Executors.callable(watch));
//                        } else {
//                            watch.run();
//                        }
//                    } else {
//                        scrapeFile.accept(file);
//                    }
//                }
//
//                if(rootPoller) {
////                    executors.getService(jobPojo, "poll", true).invokeAll(polls);
//                    l.info("Finished batch! Waiting 20 minutes for next poll", jobPojo);
//                    Thread.sleep(1200000);
//                }
//            } while (rootPoller);
//        } catch (Exception e) {
//            l.error("Could not poll folder: {}", jobPojo, e.getMessage());
//        }
//
//
//    }

    public static Path getParentPath(String path) {
        Path p = Paths.get(path).getParent();
        if(p == null) return Paths.get("");

        return p;
    }

    public static String replaceFileExtension(String scrapePath, String extension) {
        String path = scrapePath;
        path = path.substring(0,path.length() - FilenameUtils.getExtension(path).length())+extension;
        return path;
    }

//        Collection<String> paths = StringUtil.pathProduct(jobDefinition.getPaths(), jobDefinition.getFragmentFolders());
//        File fragment = FileUtil.getFirstExisting(
//                (String) job.getProcessKey(i, "required"),
//                paths,
//                jobDefinition.getBasePath(),
//                FileUtil.getParentPath(jobDefinition.getScrapeFile()).toString()
//        );

    public static void match(String glob, String location, Consumer<Path> consumer) throws IOException {

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
