package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;


/**
 * Evaluates a long timestamp in string format and checks if adding the <var>differenceMs</var>
 * is greater than the current system timestamp.
 */
@NodePlugin("0.1.0")
public final class TimestampDifference implements FunctionalNode {

    /** Long timestamp in string format */
    @FlowKey(mandatory = true)
    private final T<String> timestamp = new T<>() {};

    /** Time difference in ms */
    @FlowKey(mandatory = true) @Argument
    private Integer differenceMs;

    /** Result location */
    @FlowKey(mandatory = true)
    private final L<Boolean> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Long thatTimestampMs = Long.valueOf(o.eval(timestamp));
        long currentTimestampMs = System.currentTimeMillis();

        o.output(put, thatTimestampMs + differenceMs > currentTimestampMs);
    }
}
