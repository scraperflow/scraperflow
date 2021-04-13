package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.Io;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeIOException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads a file and joins every line with a separator.
 * Outputs a String.
 * Throws an exception if the file does not exist.
 */
@NodePlugin("0.4.0")
@Io
public final class ReadFile implements FunctionalNode {

    /** Input file path */
    @FlowKey(mandatory = true)
    private final T<String> inputFile = new T<>(){};

    /** Where the output line will be put */
    @FlowKey(defaultValue = "\"output\"")
    private final L<String> output = new L<>(){};

    /** Character encoding of the file */
    @FlowKey(defaultValue = "\"ISO_8859_1\"")
    private String charset;

    /** Join lines with this string. Can be empty. */
    @FlowKey(defaultValue = "\"\\n\"")
    private String join;

    public void modify(@NotNull final FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String file = o.eval(inputFile);

        if(!new File(file).exists()) throw new NodeIOException(n.getAddress() + ": File does not exist: " + file);

        try (Stream<String> stream = Files.lines(Paths.get(file), Charset.forName(charset))) {

            o.output(output,stream.collect(Collectors.joining(join)));
        } catch (IOException e) {
            throw new NodeIOException(e, "File IO error");
        }
    }
}
