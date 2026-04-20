import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Single-file test runner. Mirrors test.sh.
 *
 * Usage:  java Test.java
 * Assumes deps are in build/deps and main classes in build/classes.
 */
public class Test {

    static final List<String> MODS = List.of(
        "scraper.api", "scraper.utils", "scraper.core",
        "scraper.core.parsers", "scraper.core.plugins", "scraper.test"
    );
    static final String AUX = "scraper.core.testproviders";
    static final String ALL = String.join(",", MODS) + "," + AUX
        + ",org.junit.jupiter.api,org.junit.jupiter.params,org.junit.jupiter.engine";
    static final String MP  = "build/deps:build/classes:build/test-aux";

    public static void main(String[] args) throws Exception {
        // Compile the test-only aux module (real `provides` for ServiceLoader).
        mkdir("build/test-aux/" + AUX);
        var auxCmd = new ArrayList<>(List.of("javac",
            "-d", "build/test-aux/" + AUX, "-p", "build/deps:build/classes"));
        auxCmd.addAll(findJava(AUX + "/src/main/java", true));
        exec(auxCmd);

        for (var m : MODS) {
            var src = m + "/src/test/java";
            if (!Files.isDirectory(Path.of(src))) continue;
            var dst = "build/test-classes/" + m;
            mkdir(dst);

            var compile = new ArrayList<>(List.of("javac", "-d", dst, "-p", MP,
                "--patch-module", m + "=" + src,
                "--add-modules", ALL,
                "--add-reads", m + "=" + ALL));
            compile.addAll(findJava(src, false));
            exec(compile);

            var res = Path.of(m + "/src/test/resources");
            if (Files.isDirectory(res)) copyTree(res, Path.of(dst));

            // JUnit needs each test package opened for reflection.
            var run = new ArrayList<>(List.of("java", "-p", MP + ":" + dst,
                "--add-modules", "ALL-MODULE-PATH",
                "--patch-module", m + "=" + dst,
                "--add-reads", m + "=" + ALL));
            for (var pkg : packages(src)) {
                run.add("--add-opens");
                run.add(m + "/" + pkg + "=org.junit.platform.commons");
            }
            run.addAll(List.of("-m",
                "org.junit.platform.console/org.junit.platform.console.ConsoleLauncher",
                "--select-module", m, "--fail-if-no-tests"));

            System.out.println("== Testing " + m + " ==");
            exec(run);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────

    static void mkdir(String p) throws IOException { Files.createDirectories(Path.of(p)); }

    static List<String> findJava(String dir, boolean keepModuleInfo) throws IOException {
        try (var w = Files.walk(Path.of(dir))) {
            return w.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> keepModuleInfo
                            || !p.getFileName().toString().equals("module-info.java"))
                    .map(Path::toString).sorted().toList();
        }
    }

    /** Dotted package names containing a .java file under {@code root}. */
    static List<String> packages(String root) throws IOException {
        var r = Path.of(root);
        try (var w = Files.walk(r)) {
            return w.filter(p -> p.toString().endsWith(".java"))
                    .map(p -> r.relativize(p.getParent()).toString().replace('/', '.'))
                    .filter(s -> !s.isEmpty()).distinct().sorted().toList();
        }
    }

    static void copyTree(Path from, Path to) throws IOException {
        try (var w = Files.walk(from)) {
            w.forEach(p -> {
                try {
                    Path t = to.resolve(from.relativize(p));
                    if (Files.isDirectory(p)) Files.createDirectories(t);
                    else Files.copy(p, t, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) { throw new RuntimeException(e); }
            });
        }
    }

    static void exec(List<String> cmd) throws Exception {
        int rc = new ProcessBuilder(cmd).inheritIO().start().waitFor();
        if (rc != 0) System.exit(rc);
    }
}
