package scraper.core;

import scraper.annotations.node.NodePlugin;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;


/**
 * Fixes accept method for functional nodes
 */
@NodePlugin("1.0.0")
public abstract class AbstractFunctionalNode extends AbstractNode<FunctionalNode> implements FunctionalNodeContainer {
    public AbstractFunctionalNode(String instance, String graph, String node, int index) { super(instance, graph, node, index); }
}
