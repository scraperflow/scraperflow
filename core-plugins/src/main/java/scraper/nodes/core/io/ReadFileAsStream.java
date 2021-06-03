package scraper.nodes.core.io;

import scraper.annotations.*;
import scraper.api.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Reads a file and streams each line.
 */
@NodePlugin("0.2.0")
@Io
public final class ReadFileAsStream implements StreamNode {

    /** Input file path */
    @FlowKey(mandatory = true) @EnsureFile
    private final T<String> inputFile = new T<>(){};

    /** Charset of the file */
    @FlowKey(defaultValue = "\"UTF-8\"")
    private String charset;

    /** Where the output line will be put */
    @FlowKey(mandatory = true)
    private final L<String> output = new L<>(){};

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
