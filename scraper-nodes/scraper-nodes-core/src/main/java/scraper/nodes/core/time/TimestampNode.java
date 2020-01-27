package scraper.nodes.core.time;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;


/**
 * Echoes current timestamp in ms
 */
@NodePlugin("0.1.0")
public final class TimestampNode extends AbstractFunctionalNode {

    @FlowKey(defaultValue = "\"timestamp\"", output = true)
    private Template<Long> put = new Template<>(){};

    @Override
    public void modify(@NotNull final FlowMap o) {
        put.output(o, System.currentTimeMillis());
    }
}
