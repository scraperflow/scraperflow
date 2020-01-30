package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;


/**
 * Echoes current timestamp in ms
 */
@NodePlugin("0.1.0")
public final class TimestampNode implements FunctionalNode {

    @FlowKey(defaultValue = "\"timestamp\"", output = true)
    private T<Long> put = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.output(put, System.currentTimeMillis());
    }
}
