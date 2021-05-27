package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;

/**
 * No-op node used just for logging.
 */
@NodePlugin("1.0.0")
public class Log implements FunctionalNode {

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {}
}
