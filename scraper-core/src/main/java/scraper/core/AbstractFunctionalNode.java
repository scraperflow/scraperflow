package scraper.core;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.node.NodePlugin;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;


/**
 * Fixes accept method for functional nodes
 */
@NodePlugin("1.0.0")
public abstract class AbstractFunctionalNode extends AbstractNode<FunctionalNode> implements FunctionalNodeContainer {
    AbstractFunctionalNode(
            @NotNull String instance,
            @NotNull String graph,
            @Nullable String node,
            int index
    ) { super(instance, graph, node, index); }
}
