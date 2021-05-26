package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.Io;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Reads a file and streams each line.
 */
@NodePlugin("0.1.0")
@Io
public final class ReadFileAsStream implements StreamNode {

    /** Input file path */
    @FlowKey(mandatory = true) @EnsureFile
    private final T<String> inputFile = new T<>(){};

    /** Where the output line will be put */
    @FlowKey(defaultValue = "\"output\"")
    private final L<String> output = new L<>(){};

    /** Charset of the file */
    @FlowKey(defaultValue = "\"ISO_8859_1\"")
    private String charset;

    @Override
    public void process(@NotNull StreamNodeContainer n, @NotNull FlowMap o) throws NodeException {
        String file = o.eval(inputFile);

        try (Stream<String> stream = Files.lines(Paths.get(file), Charset.forName(charset))) {
            stream.forEach(line -> n.streamElement(o, output, line));
        } catch (IOException e) {
            throw new NodeException(e, "File IO error");
        }
    }
}
