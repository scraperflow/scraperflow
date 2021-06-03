package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;

/**
 * No-op node used just for logging.
 */
@NodePlugin("1.0.0")
public class Log implements FunctionalNode {

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {}
}
