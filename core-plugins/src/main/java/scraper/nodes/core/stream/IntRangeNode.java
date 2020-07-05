package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.template.L;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Int range generator
 * Example:
 * <pre>
 * type: IntRangeNode
 * from: 1
 * to: 2
 * output: page
 * </pre>
 */
@NodePlugin("0.2.0")
public final class IntRangeNode implements StreamNode {

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
        n.collect(o, List.of(o.evalLocation(output)));

        IntStream.rangeClosed(from, to).forEach(i ->{
            FlowMap copy = o.copy();
            copy.output(output, i);
            n.streamFlowMap(o, copy);
        });
    }
}
