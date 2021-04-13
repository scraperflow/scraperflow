package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.template.L;

import java.util.stream.IntStream;

/**
 * Int range generator
 * Example:
 * <pre>
 * type: IntRange
 * from: 1
 * to: 2
 * output: page
 * </pre>
 */
@NodePlugin("0.2.0")
public final class IntRange implements StreamNode {

    /** Start index, inclusive */
    @FlowKey(mandatory = true) @Argument
    private Integer from;

    /** End index, inclusive */
    @FlowKey(mandatory = true) @Argument
    private Integer to;

    /** Where the output will be put */
    @FlowKey(defaultValue = "\"i\"")
    private final L<Integer> output = new L<>(){};


    @Override
    public void process(@NotNull final StreamNodeContainer n, @NotNull final FlowMap o) {
        IntStream.rangeClosed(from, to).forEach(i ->{
            FlowMap copy = o.copy();
            copy.output(output, i);
            System.out.println("Streaming...");
            n.streamFlowMap(o, copy);
        });
    }
}
