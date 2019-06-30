package scraper.core;

import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.type.FunctionalNode;


/**
 * Fixes accept method for functional nodes
 */
@NodePlugin("1.0.0")
public abstract class AbstractFunctionalNode extends AbstractNode implements FunctionalNode {
    @Override
    public FlowMap process(FlowMap o) throws NodeException {
        start(o);
        modify(o);
        finish(o);
        return forward(o);
    }
}
