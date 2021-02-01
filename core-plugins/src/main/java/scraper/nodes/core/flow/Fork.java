package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.annotations.node.Flow;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.List;

/**
 * Forks to targets and continues the flow.
 */
@NodePlugin("0.2.0")
public final class Fork implements Node {

    /** All nodes to fork the current flow map to */
    @FlowKey(mandatory = true)
    @Flow(dependent = false, crossed = false, label = "fork")
    private final T<List<Address>> forkTargets = new T<>(){};

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        o.evalIdentity(forkTargets).forEach(target -> {
            // dispatch new flow for every goTo
            FlowMap copy = o.copy();
            n.forkDispatch(copy, target);
        });

        return o;
    }
}
