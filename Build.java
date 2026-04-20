import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.*;

/**
 * ScraperFlow Build Script — Java builds itself.
 *
 * Usage:  java Build.java [command] [args...]
 *
 * Commands:
 *   clean    Remove all build artifacts
 *   deps     Download dependencies from Maven Central
 *   compile  Compile all modules (implies deps)
 *   jar      Create module JARs (implies compile)
 *   test     Run tests (implies jar)
 *   dist     Create distribution (implies jar)
 *   build    Full build: compile + jar + test + dist
 *   run      Run ScraperFlow (implies jar); extra args passed through
 *   help     Show this help
 *
 * Requires: Java 11+
 */
public class Build {

    // ── Paths ─────────────────────────────────────────────────────────

    static final Path ROOT         = Path.of("").toAbsolutePath();
    static final Path BUILD_DIR    = ROOT.resolve("build");
    static final Path DEPS_DIR     = BUILD_DIR.resolve("deps");
    static final Path DEPS_TEST    = BUILD_DIR.resolve("deps-test");
    static final Path CLASSES_DIR  = BUILD_DIR.resolve("classes");
    static final Path TEST_CLS_DIR = BUILD_DIR.resolve("test-classes");
    static final Path TEST_AUX_DIR = BUILD_DIR.resolve("test-aux");
    static final Path JARS_DIR     = BUILD_DIR.resolve("jars");
    static final Path DIST_DIR     = BUILD_DIR.resolve("dist").resolve("scraperflow");

    // ── Maven coordinates ─────────────────────────────────────────────

    static final String MVN = "https://repo1.maven.org/maven2";

    static String depJar(String artifact, String version) {
        return artifact + "-" + version + ".jar";
    }

    static String depUrl(String group, String artifact, String version) {
        return MVN + "/" + group + "/" + artifact + "/" + version + "/" + depJar(artifact, version);
    }

    // group-path, artifact, version
    static final String[][] COMPILE_DEPS = {
        {"com/fasterxml/jackson/core",      "jackson-core",             "2.12.5"},
        {"com/fasterxml/jackson/core",      "jackson-databind",         "2.12.5"},
        {"com/fasterxml/jackson/core",      "jackson-annotations",      "2.12.5"},
        {"com/fasterxml/jackson/dataformat", "jackson-dataformat-yaml", "2.12.5"},
        {"org/yaml",                         "snakeyaml",               "1.27"},
    };

    static final String[][] TEST_DEPS = {
        {"org/junit/jupiter",  "junit-jupiter-api",       "5.7.0"},
        {"org/junit/jupiter",  "junit-jupiter-params",    "5.7.0"},
        {"org/junit/jupiter",  "junit-jupiter-engine",    "5.7.0"},
        {"org/junit/platform", "junit-platform-engine",   "1.7.0"},
        {"org/junit/platform", "junit-platform-commons",  "1.7.0"},
        {"org/junit/platform", "junit-platform-launcher", "1.7.0"},
        {"org/junit/platform", "junit-platform-console",  "1.7.0"},
        {"org/junit/platform", "junit-platform-reporting", "1.7.0"},
        {"org/opentest4j",     "opentest4j",              "1.2.0"},
        {"org/apiguardian",    "apiguardian-api",         "1.1.0"},
    };

    // ── Module definitions ────────────────────────────────────────────

    // Each module lives in a directory named after the module, with the
    // standard Maven-style layout (src/main/java, src/test/java, …).
    // javac resolves inter-module dependencies automatically.
    static final String[] MODULES = {
        "scraper.api",
        "scraper.utils",
        "scraper.core",
        "scraper.core.parsers",
        "scraper.core.plugins",
        "scraper.test",
        "scraper.app",
    };

    // Test-only auxiliary modules. Compiled and placed on the module path
    // at test time only — never shipped in dist. Used to register test-only
    // ServiceLoader providers, since patched modules can't add `provides`.
    static final String[] TEST_AUX_MODULES = {
        "scraper.core.testproviders",
    };

    // ── Entry point ───────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        var build = new Build();
        var cmd = args.length > 0 ? args[0] : "build";
        var extra = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        long t0 = System.currentTimeMillis();
        switch (cmd) {
            case "clean"   -> build.clean();
            case "deps"    -> build.deps();
            case "compile" -> { build.deps(); build.compile(); }
            case "jar"     -> { build.deps(); build.compile(); build.jar(); }
            case "test"    -> { build.deps(); build.compile(); build.jar(); build.compileTests(); build.test(); }
            case "dist"    -> { build.deps(); build.compile(); build.jar(); build.dist(); }
            case "build"   -> { build.deps(); build.compile(); build.jar(); build.compileTests(); build.test(); build.dist(); }
            case "run"     -> { build.deps(); build.compile(); build.jar(); build.run(extra); }
            case "help"    -> { build.help(); return; }
            default        -> { err("Unknown command: " + cmd); build.help(); System.exit(1); }
        }
        long elapsed = System.currentTimeMillis() - t0;
        System.out.printf("%n\u001b[32m✓ %s completed in %.1fs\u001b[0m%n", cmd, elapsed / 1000.0);
    }

    final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();

    // ── clean ─────────────────────────────────────────────────────────

    void clean() throws IOException {
        banner("clean");
        if (Files.exists(BUILD_DIR)) {
            deleteTree(BUILD_DIR);
            log("Deleted " + BUILD_DIR);
        } else {
            log("Nothing to clean.");
        }
    }

    // ── deps ──────────────────────────────────────────────────────────

    void deps() throws Exception {
        banner("deps");
        Files.createDirectories(DEPS_DIR);
        Files.createDirectories(DEPS_TEST);

        int downloaded = 0;
        for (var d : COMPILE_DEPS) {
            if (download(d[0], d[1], d[2], DEPS_DIR)) downloaded++;
        }
        for (var d : TEST_DEPS) {
            if (download(d[0], d[1], d[2], DEPS_TEST)) downloaded++;
        }

        if (downloaded == 0) log("All dependencies already present.");
        else log("Downloaded " + downloaded + " artifact(s).");
    }

    /** Returns true if a download actually happened. */
    boolean download(String group, String artifact, String version, Path dir) throws Exception {
        String jar = depJar(artifact, version);
        Path target = dir.resolve(jar);
        if (Files.exists(target)) return false;

        String url = depUrl(group, artifact, version);
        log("  Downloading " + jar + " ...");
        var req = HttpRequest.newBuilder(URI.create(url)).GET().build();
        var resp = http.send(req, BodyHandlers.ofFile(target));
        if (resp.statusCode() != 200) {
            Files.deleteIfExists(target);
            throw new RuntimeException("Failed to download " + url + " (HTTP " + resp.statusCode() + ")");
        }
        return true;
    }

    // ── compile ───────────────────────────────────────────────────────

    void compile() throws Exception {
        banner("compile");
        for (var moduleName : MODULES) {
            compileModule(moduleName);
        }
    }

    void compileModule(String moduleName) throws Exception {
        Path src = ROOT.resolve(moduleName).resolve("src/main/java");
        Path out = CLASSES_DIR.resolve(moduleName);

        List<String> sources = findJava(src);
        if (sources.isEmpty()) {
            err("  No sources found for " + moduleName + " in " + src);
            System.exit(1);
        }

        // Up-to-date check: skip if all sources are older than the marker file
        Path marker = BUILD_DIR.resolve(moduleName + ".compiled");
        if (Files.exists(marker) && isUpToDate(sources, marker)) {
            log("  " + moduleName + " (up to date)");
            return;
        }

        Files.createDirectories(out);
        log("  Compiling " + moduleName + " (" + sources.size() + " files)");

        // Module path: previously compiled modules (exploded) + external deps
        String mp = modulePath(CLASSES_DIR, JARS_DIR, DEPS_DIR);

        // Write source list to an argfile (avoids command-line length limits)
        Path argfile = BUILD_DIR.resolve(moduleName + ".args");
        Files.createDirectories(argfile.getParent());
        Files.writeString(argfile, String.join("\n", sources));

        var cmd = new ArrayList<>(List.of(
            "javac",
            "--module-path", mp,
            "-d", out.toString(),
            "@" + argfile
        ));

        exec(cmd);

        // Copy main resources into classes dir if they exist
        Path res = ROOT.resolve(moduleName).resolve("src/main/resources");
        if (Files.isDirectory(res)) {
            copyTree(res, out);
        }

        // Touch marker for up-to-date tracking
        Files.writeString(marker, "");
    }

    // ── jar ───────────────────────────────────────────────────────────

    void jar() throws Exception {
        banner("jar");
        Files.createDirectories(JARS_DIR);
        for (var moduleName : MODULES) {
            jarModule(moduleName);
        }
    }

    void jarModule(String moduleName) throws Exception {
        Path classes = CLASSES_DIR.resolve(moduleName);
        Path jarFile = JARS_DIR.resolve(moduleName + ".jar");
        Path compileMarker = BUILD_DIR.resolve(moduleName + ".compiled");

        // Skip if JAR is newer than last compilation
        if (Files.exists(jarFile) && Files.exists(compileMarker)
                && Files.getLastModifiedTime(jarFile).toMillis()
                   >= Files.getLastModifiedTime(compileMarker).toMillis()) {
            log("  " + jarFile.getFileName() + " (up to date)");
            return;
        }

        log("  Packaging " + jarFile.getFileName());

        var cmd = List.of(
            "jar", "--create",
            "--file", jarFile.toString(),
            "-C", classes.toString(), "."
        );
        exec(cmd);
    }

    // ── compileTests ──────────────────────────────────────────────────

    void compileTests() throws Exception {
        banner("compile-tests");

        // Aux modules first, so they're on the module path when patched
        // tests compile and reference their classes.
        for (var moduleName : TEST_AUX_MODULES) {
            compileAuxModule(moduleName);
        }
        for (var moduleName : MODULES) {
            compilePatchedTest(moduleName);
        }
    }

    /** Compile a test-only auxiliary module into TEST_AUX_DIR. */
    void compileAuxModule(String moduleName) throws Exception {
        Path src = ROOT.resolve(moduleName).resolve("src/main/java");
        if (!Files.isDirectory(src)) return;

        List<String> sources = findJava(src);
        if (sources.isEmpty()) return;

        Path out = TEST_AUX_DIR.resolve(moduleName);
        Files.createDirectories(out);

        log("  Compiling test-aux module " + moduleName + " (" + sources.size() + " files)");

        String mp = modulePath(JARS_DIR, DEPS_DIR);

        Path argfile = BUILD_DIR.resolve(moduleName + "-aux.args");
        Files.writeString(argfile, String.join("\n", sources));

        exec(List.of(
            "javac",
            "--module-path", mp,
            "-d", out.toString(),
            "@" + argfile
        ));
    }

    /** Compile tests using --patch-module. Any test module-info.java is ignored. */
    void compilePatchedTest(String moduleName) throws Exception {
        Path src = ROOT.resolve(moduleName).resolve("src/test/java");
        if (!Files.isDirectory(src)) return;

        List<String> sources = findJava(src).stream()
            .filter(s -> !s.endsWith("module-info.java"))
            .toList();
        if (sources.isEmpty()) return;

        Path out = TEST_CLS_DIR.resolve(moduleName);
        Files.createDirectories(out);

        log("  Compiling tests for " + moduleName + " (patched, " + sources.size() + " files)");

        String mp = modulePath(JARS_DIR, DEPS_DIR, DEPS_TEST, TEST_AUX_DIR);

        Path argfile = BUILD_DIR.resolve(moduleName + "-test.args");
        Files.writeString(argfile, String.join("\n", sources));

        // Make every module on the path visible to the patched test compile,
        // and allow the patched module to read everything.
        String allModules = Stream.concat(Arrays.stream(MODULES), Arrays.stream(TEST_AUX_MODULES))
                .collect(Collectors.joining(","))
                + ",org.junit.jupiter.api,org.junit.jupiter.params";

        exec(List.of(
            "javac",
            "--module-path", mp,
            "--patch-module", moduleName + "=" + src,
            "--add-modules", allModules,
            "--add-reads", moduleName + "=" + allModules,
            "-d", out.toString(),
            "@" + argfile
        ));

        // Copy test resources
        Path res = ROOT.resolve(moduleName).resolve("src/test/resources");
        if (Files.isDirectory(res)) {
            copyTree(res, out);
        }
    }

    // ── test ──────────────────────────────────────────────────────────

    void test() throws Exception {
        banner("test");

        boolean anyFailed = false;
        for (var moduleName : MODULES) {
            if (!runPatchedTest(moduleName)) anyFailed = true;
        }

        if (anyFailed) {
            err("Some tests FAILED.");
            System.exit(1);
        }
        log("All tests passed.");
    }

    /** Run tests for a patched module. Returns true if passed. */
    boolean runPatchedTest(String moduleName) throws Exception {
        Path testClasses = TEST_CLS_DIR.resolve(moduleName);
        if (!Files.isDirectory(testClasses)) {
            log("  Skipping " + moduleName + " (no test classes)");
            return true;
        }

        log("  Running tests: " + moduleName + " (patched)");

        String mp = modulePath(JARS_DIR, DEPS_DIR, DEPS_TEST, TEST_AUX_DIR);

        // Build patch-module value: include test classes and test resources
        String patchValue = moduleName + "=" + testClasses;
        Path testRes = ROOT.resolve(moduleName).resolve("src/test/resources");
        if (Files.isDirectory(testRes)) {
            patchValue += ":" + testRes;
        }

        // Let the patched module read everything on the module path, so tests
        // can reach sibling main modules and JUnit regardless of the main
        // module-info.java's explicit requires.
        String allReads = Stream.concat(Arrays.stream(MODULES), Arrays.stream(TEST_AUX_MODULES))
                .collect(Collectors.joining(","))
                + ",org.junit.jupiter.api,org.junit.jupiter.params,org.junit.jupiter.engine";

        var cmd = new ArrayList<>(List.of(
            "java",
            "--module-path", mp,
            "--add-modules", "ALL-MODULE-PATH",
            "--patch-module", patchValue,
            "--add-reads", moduleName + "=" + allReads
        ));

        // Open every package containing tests so JUnit can reflect into them.
        // JPMS has no wildcard for --add-opens, so we enumerate them from the
        // test source tree.
        for (String pkg : discoverPackages(ROOT.resolve(moduleName).resolve("src/test/java"))) {
            cmd.add("--add-opens");
            cmd.add(moduleName + "/" + pkg + "=org.junit.platform.commons");
        }

        cmd.addAll(List.of(
            "-m", "org.junit.platform.console/org.junit.platform.console.ConsoleLauncher",
            "--select-module", moduleName,
            "--fail-if-no-tests"
        ));

        return execQuiet(cmd) == 0;
    }

    // ── dist ──────────────────────────────────────────────────────────

    void dist() throws Exception {
        banner("dist");
        if (Files.exists(DIST_DIR)) deleteTree(DIST_DIR);

        Path libDir = DIST_DIR.resolve("lib");
        Files.createDirectories(libDir);

        // Copy module JARs
        for (var moduleName : MODULES) {
            Path jar = JARS_DIR.resolve(moduleName + ".jar");
            Files.copy(jar, libDir.resolve(jar.getFileName()));
        }

        // Copy external dependency JARs
        try (var stream = Files.list(DEPS_DIR)) {
            for (Path dep : stream.filter(p -> p.toString().endsWith(".jar")).toList()) {
                Files.copy(dep, libDir.resolve(dep.getFileName()));
            }
        }

        // Create ext/ and var/ dirs (used by the launch script)
        Files.createDirectories(DIST_DIR.resolve("ext"));
        Files.createDirectories(DIST_DIR.resolve("var"));

        // Copy launch script from dist/
        Path launchSrc = ROOT.resolve("dist").resolve("scraperflow");
        Path launchDst = DIST_DIR.resolve("scraperflow");
        if (Files.exists(launchSrc)) {
            Files.copy(launchSrc, launchDst);
            launchDst.toFile().setExecutable(true);
        } else {
            // Generate a launch script
            Files.writeString(launchDst, """
                #!/bin/bash
                DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
                java --add-modules scraper.core.parsers --add-modules ALL-MODULE-PATH \\
                     -p "${DIR}/ext:${DIR}/lib:${DIR}/var" \\
                     -m scraper.app/scraper.app.Scraper "$@"
                """);
            launchDst.toFile().setExecutable(true);
        }

        log("Distribution created at: " + DIST_DIR);
    }

    // ── run ───────────────────────────────────────────────────────────

    void run(String[] args) throws Exception {
        banner("run");

        String mp = modulePath(JARS_DIR, DEPS_DIR);

        var cmd = new ArrayList<>(List.of(
            "java",
            "--module-path", mp,
            "--add-modules", "ALL-MODULE-PATH",
            "-m", "scraper.app/scraper.app.Scraper"
        ));
        cmd.addAll(Arrays.asList(args));

        log("  " + String.join(" ", cmd));
        System.out.println();

        var pb = new ProcessBuilder(cmd)
            .inheritIO()
            .directory(ROOT.toFile());
        int rc = pb.start().waitFor();
        if (rc != 0) System.exit(rc);
    }

    // ── help ──────────────────────────────────────────────────────────

    void help() {
        System.out.println("""

            ScraperFlow Build Script — Java builds itself.

            Usage:  java Build.java [command] [args...]

            Commands:
              clean    Remove all build artifacts
              deps     Download dependencies from Maven Central
              compile  Compile all modules (implies deps)
              jar      Create module JARs (implies compile)
              test     Run tests (implies jar)
              dist     Create distribution (implies jar)
              build    Full build: compile + jar + test + dist  [default]
              run      Run ScraperFlow (implies jar); extra args passed through
              help     Show this help

            Examples:
              java Build.java                 # full build
              java Build.java compile         # just compile
              java Build.java run help        # compile & run with "help" arg
              java Build.java clean compile   # won't work — one command at a time
            """);
    }

    // ── Utilities ─────────────────────────────────────────────────────

    /** Discover all packages that contain .java files under a source root. */
    List<String> discoverPackages(Path srcRoot) throws IOException {
        if (!Files.isDirectory(srcRoot)) return List.of();
        try (var walk = Files.walk(srcRoot)) {
            return walk
                .filter(p -> p.toString().endsWith(".java"))
                .map(p -> srcRoot.relativize(p.getParent()).toString().replace(File.separatorChar, '.'))
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .toList();
        }
    }

    /** Find all .java files under a directory. */
    List<String> findJava(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) return List.of();
        try (var walk = Files.walk(dir)) {
            return walk
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toString)
                .sorted()
                .toList();
        }
    }

    /** Check if all sources are older than the marker file. */
    boolean isUpToDate(List<String> sources, Path marker) {
        try {
            long markerTime = Files.getLastModifiedTime(marker).toMillis();
            for (String src : sources) {
                if (Files.getLastModifiedTime(Path.of(src)).toMillis() > markerTime) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Build a module-path string from directories. */
    String modulePath(Path... dirs) {
        return Arrays.stream(dirs)
            .filter(Files::isDirectory)
            .map(Path::toString)
            .collect(Collectors.joining(":"));
    }

    /** Execute a command, inheriting stdout/stderr. Exits on failure. */
    void exec(List<String> cmd) throws Exception {
        var pb = new ProcessBuilder(cmd)
            .inheritIO()
            .directory(ROOT.toFile());
        int rc = pb.start().waitFor();
        if (rc != 0) {
            err("Command failed (exit " + rc + "): " + cmd.get(0));
            System.exit(rc);
        }
    }

    /** Execute a command, printing output. Returns exit code (does not exit). */
    int execQuiet(List<String> cmd) throws Exception {
        var pb = new ProcessBuilder(cmd)
            .inheritIO()
            .directory(ROOT.toFile());
        return pb.start().waitFor();
    }

    /** Recursively delete a directory tree. */
    void deleteTree(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /** Recursively copy a directory tree. */
    void copyTree(Path src, Path dst) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(dst.resolve(src.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static void banner(String phase) {
        System.out.printf("%n\u001b[1m── %s ──\u001b[0m%n", phase);
    }

    static void log(String msg) {
        System.out.println(msg);
    }

    static void err(String msg) {
        System.out.println("\u001b[31m" + msg + "\u001b[0m");
    }
}
