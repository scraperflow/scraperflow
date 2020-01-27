package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.AbstractNode;
import scraper.core.Template;


/**
 * Executes node target given specified difference in ms
 *
 * @see AbstractNode
 * @since 0.1
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class TimestampDifferenceNode extends AbstractFunctionalNode {

    /** Long timestamp in string format */
    @FlowKey(mandatory = true)
    private Template<String> timestamp = new Template<>() {};

    /** time difference in ms */
    @FlowKey(mandatory = true) @Argument
    private Integer differenceMs;

    @FlowKey(mandatory = true, output = true)
    private Template<Boolean> put = new Template<>(){};

    @Override
    public void modify(@NotNull final FlowMap o) {
        Long thatTimestampMs = Long.valueOf(timestamp.eval(o));
        long currentTimestampMs = System.currentTimeMillis();

        put.output(o, thatTimestampMs + differenceMs > currentTimestampMs);
    }
}
