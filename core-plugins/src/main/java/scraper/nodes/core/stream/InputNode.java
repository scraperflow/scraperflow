package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.template.L;

import java.util.Scanner;

/**
 * Processes string input stream from the environment
 */
@NodePlugin("0.1.0")
public final class InputNode implements StreamNode {

    /** String output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> put = new L<>(){};

    /** Token delimiter (default: any line break) */
    @FlowKey(defaultValue = "\"\\\\R\"") @Argument
    private String delimiter;

    @Override
    public void process(@NotNull final StreamNodeContainer n, @NotNull final FlowMap o) {
        Scanner s = new Scanner(System.in);
        s.useDelimiter(delimiter);
        s.tokens().forEach(t -> n.streamElement(o, put, t));
    }
}
