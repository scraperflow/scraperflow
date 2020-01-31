package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 */
@NodePlugin("1.0.0")
public final class ReadFileNode implements FunctionalNode {

    /** Input file path */
    @FlowKey(mandatory = true) @EnsureFile
    private final T<String> inputFile = new T<>(){};

    /** Where the output line will be put */
    @FlowKey(defaultValue = "\"output\"", output = true)
    private T<String> output = new T<>(){};

    public void modify(@NotNull FunctionalNodeContainer n, FlowMap o) throws NodeException {
        String file = o.eval(inputFile);

        try (Stream<String> stream = Files.lines(Paths.get(file), Charset.forName("ISO_8859_1"))) {
            o.output(output,stream.collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new NodeException(e, "File IO error");
        }
    }
}
