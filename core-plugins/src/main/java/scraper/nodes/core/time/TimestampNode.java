package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;


/**
 * Echoes current system timestamp in ms.
 */
@NodePlugin("0.1.0")
public final class TimestampNode implements FunctionalNode {

    /** Result location */
    @FlowKey(defaultValue = "\"timestamp\"")
    private final L<Long> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.output(put, System.currentTimeMillis());
    }
}
