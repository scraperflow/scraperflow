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
import scraper.util.NodeUtil;

import java.util.List;

/**
 *
 */
@NodePlugin("0.1.0")
public final class ForkNode implements Node {

    /** All nodes to fork the current flow map to */
    @FlowKey(mandatory = true)
    private T<List<Address>> forkTargets = new T<>() {};

    @NotNull
    @Override
    public FlowMap process(NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        o.evalIdentity(forkTargets).forEach(target -> {
            // dispatch new flow for every goTo
            FlowMap copy = NodeUtil.flowOf(o);
            n.forkDispatch(copy, target);
        });

        return n.forward(o);
    }
}
