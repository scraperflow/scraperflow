package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;
import scraper.core.AbstractNode;


/**
 * Executes node target given specified difference in ms
 *
 * @see AbstractNode
 * @since 0.1
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class TimestampDifferenceNode implements FunctionalNode {

    /** Long timestamp in string format */
    @FlowKey(mandatory = true)
    private T<String> timestamp = new T<>() {};

    /** time difference in ms */
    @FlowKey(mandatory = true) @Argument
    private Integer differenceMs;

    @FlowKey(mandatory = true, output = true)
    private T<Boolean> put = new T<>(){};

    @Override
    public void modify(FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Long thatTimestampMs = Long.valueOf(o.eval(timestamp));
        long currentTimestampMs = System.currentTimeMillis();

        o.output(put, thatTimestampMs + differenceMs > currentTimestampMs);
    }
}
