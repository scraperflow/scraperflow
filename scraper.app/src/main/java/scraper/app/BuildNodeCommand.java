package scraper.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@code scraperflow build-node <Foo.java> [--out <dir>]} — compiles a helper
 * node source file into a single-file native ELF that follows the
 * {@code SubprocessNode} protocol.
 *
 * <p>Everything needed to compile lives inside the scraperflow binary: the
 * wrapper template, the reflect-config fragment, and the classpath jars for
 * {@code scraper.*} + Jackson. These are extracted to a scratch dir on each
 * invocation and cleaned up on exit. External toolchain requirements are the
 * same as {@code native.sh}: {@code javac}, {@code native-image}, and
 * {@code tar} must be on {@code PATH}.
 */
public final class BuildNodeCommand {

    private BuildNodeCommand() {}

    private static final String LIB_RESOURCE = "/scraper/app/build-libs.tar";

    public static int run(String[] args) throws Exception {
        String src = null;
        String outDir = ".";
        for (int k = 1; k < args.length; k++) {
            switch (args[k]) {
                case "--out":
                    if (k + 1 >= args.length) { usage(); return 2; }
                    outDir = args[++k]; break;
                default:
                    if (src == null) src = args[k];
                    else { usage(); return 2; }
            }
        }
        if (src == null) { usage(); return 2; }
        Path srcPath = Path.of(src).toAbsolutePath();
        if (!Files.isRegularFile(srcPath)) {
            System.err.println("not a file: " + srcPath);
            return 2;
        }
        if (!srcPath.getFileName().toString().endsWith(".java")) {
            System.err.println("source must end in .java: " + srcPath);
            return 2;
        }
        String fileName = srcPath.getFileName().toString();
        String className = fileName.substring(0, fileName.length() - ".java".length());
        Path out = Path.of(outDir).toAbsolutePath();

        List<String> problems = preflight(out);
        if (!problems.isEmpty()) {
            System.err.println("scraperflow build-node: preflight failed:");
            for (String p : problems) System.err.println("  - " + p);
            return 3;
        }

        Files.createDirectories(out);
        Path stage = Files.createTempDirectory("scraperflow-build-node.");
        try {
            Path libs = stage.resolve("libs");
            Files.createDirectories(libs);
            extractLibsTarTo(libs);

            Path stagedSrc = stage.resolve(className + ".java");
            Files.copy(srcPath, stagedSrc);

            String wrapperName = className + "SubprocessMain";
            Path wrapperSrc = stage.resolve(wrapperName + ".java");
            Files.writeString(wrapperSrc, renderWrapper(className, wrapperName));

            Path classes = stage.resolve("classes");
            Files.createDirectories(classes);
            Path niMeta = stage.resolve("META-INF/native-image");
            Files.createDirectories(niMeta);
            Files.writeString(niMeta.resolve("reflect-config.json"),
                    renderReflectConfig(className));

            String cp = classpath(libs);

            run(stage, "javac", "-d", classes.toString(), "-cp", cp,
                    stagedSrc.toString(), wrapperSrc.toString());

            run(out, "native-image",
                    "-cp", classes + ":" + stage + ":" + cp,
                    "-H:Name=" + className,
                    "-H:ReflectionConfigurationFiles="
                            + niMeta.resolve("reflect-config.json"),
                    "--no-fallback",
                    "--initialize-at-build-time=com.fasterxml.jackson",
                    wrapperName);
        } finally {
            deleteRecursive(stage);
        }

        System.out.println();
        System.out.println("built: " + out.resolve(className));
        return 0;
    }

    private static void usage() {
        System.err.println("usage: scraperflow build-node <Foo.java> [--out <dir>]");
    }

    /** Runs all checks, accumulates failures, returns them as human-readable
        messages. Empty list means all good. No side effects. */
    private static List<String> preflight(Path out) {
        List<String> problems = new ArrayList<>();

        for (String exe : List.of("javac", "native-image", "tar")) {
            if (!onPath(exe)) {
                problems.add(exe + " not on PATH" + hintFor(exe));
            }
        }

        if (BuildNodeCommand.class.getResource(LIB_RESOURCE) == null) {
            problems.add("build-libs.tar resource missing from this binary — "
                    + "it was built without node-builder support "
                    + "(check native.sh).");
        }

        if (Files.exists(out) && !Files.isDirectory(out)) {
            problems.add("--out path exists but is not a directory: " + out);
        } else if (Files.exists(out) && !Files.isWritable(out)) {
            problems.add("--out directory not writable: " + out);
        } else if (!Files.exists(out)) {
            Path parent = out.getParent();
            if (parent != null && Files.exists(parent) && !Files.isWritable(parent)) {
                problems.add("--out parent not writable (can't create " + out + ")");
            }
        }

        return problems;
    }

    private static boolean onPath(String exe) {
        String path = System.getenv("PATH");
        if (path == null) return false;
        for (String entry : path.split(":")) {
            if (Files.isExecutable(Path.of(entry, exe))) return true;
        }
        return false;
    }

    private static String hintFor(String exe) {
        switch (exe) {
            case "javac":        return " (install a JDK 21+)";
            case "native-image": return " (install GraalVM 21+, e.g. `sdk install java 25.0.2-graal`)";
            case "tar":          return "";
            default:             return "";
        }
    }

    private static void extractLibsTarTo(Path dir) throws IOException, InterruptedException {
        try (InputStream in = BuildNodeCommand.class.getResourceAsStream(LIB_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "build-libs.tar resource missing — this scraperflow " +
                        "binary wasn't built with node-builder support " +
                        "(check native.sh).");
            }
            Process p = new ProcessBuilder("tar", "xf", "-", "-C", dir.toString())
                    .redirectError(ProcessBuilder.Redirect.INHERIT).start();
            in.transferTo(p.getOutputStream());
            p.getOutputStream().close();
            int rc = p.waitFor();
            if (rc != 0) throw new IOException("tar exited " + rc);
        }
    }

    private static String classpath(Path libs) throws IOException {
        try (Stream<Path> s = Files.list(libs)) {
            return s.filter(p -> p.getFileName().toString().endsWith(".jar"))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .map(Path::toString)
                    .collect(Collectors.joining(":"));
        }
    }

    private static void run(Path cwd, String... cmd) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(cmd)
                .directory(cwd.toFile())
                .inheritIO().start();
        int rc = p.waitFor();
        if (rc != 0) {
            throw new IOException(cmd[0] + " exited " + rc
                    + " (args: " + Arrays.toString(cmd) + ")");
        }
    }

    private static void deleteRecursive(Path p) {
        if (!Files.exists(p)) return;
        try (Stream<Path> walk = Files.walk(p)) {
            List<Path> all = walk.sorted(Comparator.reverseOrder())
                    .collect(Collectors.toCollection(ArrayList::new));
            for (Path q : all) Files.deleteIfExists(q);
        } catch (IOException ignored) {}
    }

    // ------------------------------------------------------------------
    // Templates
    // ------------------------------------------------------------------

    private static String renderReflectConfig(String className) {
        return "[\n"
                + "  { \"name\": \"" + className + "\", \"allDeclaredFields\": true, "
                + "\"allDeclaredConstructors\": true, \"allDeclaredMethods\": true },\n"
                + "  { \"name\": \"scraper.core.SubprocessNode\", \"allDeclaredFields\": true, "
                + "\"allDeclaredConstructors\": true },\n"
                + "  { \"name\": \"java.util.LinkedHashMap\", \"allDeclaredConstructors\": true },\n"
                + "  { \"name\": \"java.util.HashMap\", \"allDeclaredConstructors\": true }\n"
                + "]\n";
    }

    private static String renderWrapper(String className, String wrapperName) {
        return WRAPPER_TEMPLATE
                .replace("__WRAPPER__", wrapperName)
                .replace("__CLASS__", className);
    }

    /** Mirror of the old {@code node-build.sh} heredoc. {@code __CLASS__} and
        {@code __WRAPPER__} are substituted per invocation. Keep in sync with
        {@code SubprocessNode} and {@code TypeNameParser}'s schema contract. */
    private static final String WRAPPER_TEMPLATE =
            "import com.fasterxml.jackson.databind.ObjectMapper;\n" +
            "import scraper.annotations.FlowKey;\n" +
            "import scraper.api.FlowMap;\n" +
            "import scraper.api.FunctionalNodeContainer;\n" +
            "import scraper.api.L;\n" +
            "import scraper.api.T;\n" +
            "import scraper.api.flow.impl.FlowMapImpl;\n" +
            "import scraper.test.MockContainer;\n" +
            "import scraper.test.MockInstance;\n" +
            "import scraper.util.NodeUtil;\n" +
            "\n" +
            "import java.lang.reflect.Field;\n" +
            "import java.lang.reflect.GenericArrayType;\n" +
            "import java.lang.reflect.ParameterizedType;\n" +
            "import java.lang.reflect.Type;\n" +
            "import java.lang.reflect.TypeVariable;\n" +
            "import java.lang.reflect.WildcardType;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.LinkedHashMap;\n" +
            "import java.util.LinkedHashSet;\n" +
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "import java.util.Set;\n" +
            "\n" +
            "public class __WRAPPER__ {\n" +
            "    private static final ObjectMapper JSON = new ObjectMapper();\n" +
            "\n" +
            "    public static void main(String[] args) throws Exception {\n" +
            "        if (args.length == 1 && args[0].equals(\"--schema\")) {\n" +
            "            JSON.writeValue(System.out, schema());\n" +
            "            System.out.flush();\n" +
            "            return;\n" +
            "        }\n" +
            "\n" +
            "        @SuppressWarnings(\"unchecked\")\n" +
            "        Map<String, Object> req = JSON.readValue(System.in, Map.class);\n" +
            "        @SuppressWarnings(\"unchecked\")\n" +
            "        Map<String, Object> flowData = (Map<String, Object>) req.getOrDefault(\"flowMap\", Map.of());\n" +
            "        @SuppressWarnings(\"unchecked\")\n" +
            "        Map<String, Object> rawSpec = (Map<String, Object>) req.getOrDefault(\"spec\", Map.of());\n" +
            "\n" +
            "        Map<String, Object> spec = new HashMap<>(rawSpec);\n" +
            "        spec.putIfAbsent(\"type\", \"__CLASS__\");\n" +
            "\n" +
            "        __CLASS__ node = new __CLASS__();\n" +
            "        FunctionalNodeContainer container = new MockContainer(node, spec);\n" +
            "        MockInstance instance = new MockInstance(container, spec);\n" +
            "\n" +
            "        NodeUtil.initFields(node, spec, Map.of(), Map.of());\n" +
            "        node.init(container, instance);\n" +
            "\n" +
            "        FlowMapImpl flow = (FlowMapImpl) FlowMapImpl.origin(flowData);\n" +
            "        node.modify(container, flow);\n" +
            "\n" +
            "        JSON.writeValue(System.out, Map.of(\"flowMap\", flow.getPrivateMap()));\n" +
            "        System.out.flush();\n" +
            "    }\n" +
            "\n" +
            "    private static Map<String, Object> schema() throws Exception {\n" +
            "        Object probe = new __CLASS__();\n" +
            "        List<Map<String, String>> inputs = new ArrayList<>();\n" +
            "        List<Map<String, String>> outputs = new ArrayList<>();\n" +
            "        Set<String> typeVars = new LinkedHashSet<>();\n" +
            "        for (Field f : __CLASS__.class.getDeclaredFields()) {\n" +
            "            if (f.getAnnotation(FlowKey.class) == null) continue;\n" +
            "            f.setAccessible(true);\n" +
            "            Object val = f.get(probe);\n" +
            "            Type type;\n" +
            "            if (val instanceof T) type = ((T<?>) val).get();\n" +
            "            else if (val instanceof L) type = ((L<?>) val).get();\n" +
            "            else continue;\n" +
            "            Map<String, String> entry = new LinkedHashMap<>();\n" +
            "            entry.put(\"name\", f.getName());\n" +
            "            entry.put(\"type\", type.getTypeName());\n" +
            "            (val instanceof L ? outputs : inputs).add(entry);\n" +
            "            collectTypeVars(type, typeVars);\n" +
            "        }\n" +
            "        Map<String, Object> out = new LinkedHashMap<>();\n" +
            "        out.put(\"inputs\", inputs);\n" +
            "        out.put(\"outputs\", outputs);\n" +
            "        out.put(\"typeVariables\", new ArrayList<>(typeVars));\n" +
            "        return out;\n" +
            "    }\n" +
            "\n" +
            "    private static void collectTypeVars(Type t, Set<String> acc) {\n" +
            "        if (t instanceof TypeVariable) {\n" +
            "            acc.add(((TypeVariable<?>) t).getName());\n" +
            "        } else if (t instanceof ParameterizedType) {\n" +
            "            for (Type a : ((ParameterizedType) t).getActualTypeArguments()) collectTypeVars(a, acc);\n" +
            "        } else if (t instanceof GenericArrayType) {\n" +
            "            collectTypeVars(((GenericArrayType) t).getGenericComponentType(), acc);\n" +
            "        } else if (t instanceof WildcardType) {\n" +
            "            for (Type b : ((WildcardType) t).getUpperBounds()) collectTypeVars(b, acc);\n" +
            "            for (Type b : ((WildcardType) t).getLowerBounds()) collectTypeVars(b, acc);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
}
