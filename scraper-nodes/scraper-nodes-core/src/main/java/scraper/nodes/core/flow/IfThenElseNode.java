package scraper.nodes.core.flow;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;
import scraper.core.AbstractNode;

/**
 * Provides if-then-else routing
 *
 * @see AbstractNode
 * @since 0.1
 * @author Albert Schimpf
 */
@NodePlugin("0.1.0")
public final class IfThenElseNode implements Node {

    @FlowKey(mandatory = true) @NotNull
    private T<Boolean> condition = new T<>(){};

    @FlowKey
    private Address trueTarget;

    @FlowKey
    private Address falseTarget;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        Boolean condition = o.eval(this.condition);

        if(condition) {
            if(trueTarget != null)
                return n.eval(o, trueTarget);
        } else {
            if(falseTarget != null)
                return n.eval(o, falseTarget);
        }

        return o;
    }
}
