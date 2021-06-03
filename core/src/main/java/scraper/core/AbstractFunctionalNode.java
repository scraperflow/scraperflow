package scraper.core;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.NodePlugin;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;


/**
 * No special configuration for functional nodes available.
 */
@NodePlugin("0.1.0")
public abstract class AbstractFunctionalNode extends AbstractNode<FunctionalNode> implements FunctionalNodeContainer {
    AbstractFunctionalNode(
            @NotNull String instance,
            @NotNull String graph,
            @Nullable String node,
            int index
    ) { super(instance, graph, node, index); }
}
