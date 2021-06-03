package scraper.nodes.core.io;

import scraper.annotations.NotNull;
import scraper.annotations.EnsureFile;
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
