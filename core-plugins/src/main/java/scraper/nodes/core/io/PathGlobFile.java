package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.StreamNodeContainer;
import scraper.api.StreamNode;
import scraper.api.L;
import scraper.api.T;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;

/**
 * Provides a path glob starting from a given root and streams all matches.
 * Example:
 * <pre>
 * type: PathGlobFileNode
 * root: .
 * glob: "glob:**&#47;*.java"
 * </pre>
 */
@NodePlugin("0.2.1")
@Io
public final class PathGlobFile implements StreamNode {

    /** Where the output file path will be put. */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> output = new L<>(){};

    /** Where the output filename will be put. */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> filename = new L<>(){};

    /** Syntax and pattern, see Javas PathMatcher.getPathMatcher documentation. */
    @FlowKey(mandatory = true)
    private final T<String> glob = new T<>(){};

    /** The root folder from where to start */
    @FlowKey(mandatory = true)
    private final T<String> root = new T<>(){};

    /** Includes the root as a match or not */
    @FlowKey(defaultValue = "false")
    private Boolean includeRoot;

    @Override
    public void process(@NotNull StreamNodeContainer n, @NotNull FlowMap o) throws NodeException {
        String glob = o.eval(this.glob);
        String root = o.eval(this.root);

        try {
            match(root, glob, root, (matchedStringPath, filename) -> {
                FlowMap copy = o.copy();
                copy.output(output, matchedStringPath);
                copy.output(this.filename, filename);
                n.streamFlowMap(o, copy);
            });
        } catch (IOException e) {
            throw new NodeException(e, "Path error");
        }
    }

    private void match(String root, String glob, String location, BiConsumer<String, String> consumer) throws IOException, NodeException {

        final PathMatcher pathMatcher;
        try {
            pathMatcher = FileSystems.getDefault().getPathMatcher(glob);
        } catch (Exception e) {
            throw new NodeException(e, "Bad glob definition: " + glob);
        }

        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path path,
                                             BasicFileAttributes attrs) {
                if (pathMatcher.matches(path)) {
                    if(includeRoot) {
                        consumer.accept(path.toString(), path.getFileName().toString());
                    } else {
                        consumer.accept(path.toString().substring(root.length()), path.getFileName().toString());
                    }
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
