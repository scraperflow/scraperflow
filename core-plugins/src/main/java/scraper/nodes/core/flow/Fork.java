package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.annotations.node.Flow;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Forks to a given list of addresses.
 * Join by joinKey is possible.
 */
@NodePlugin("0.4.0")
public final class Fork implements Node {

    /** All nodes to fork the current flow map to */
    @FlowKey(mandatory = true)
    @Flow(label = "fork")
    private final T<List<Address>> forkTargets = new T<>(){};

    /** Key which can be used to join all emitted flows */
    @FlowKey(defaultValue = "\"_\"")
    private final L<JoinKey> joinKey = new L<>(){};

    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        List<Address> targets = o.evalIdentity(forkTargets);
        int uid = new Random().nextInt();
        AtomicInteger current = new AtomicInteger();
        o.evalIdentity(forkTargets).forEach(target -> {
            FlowMap copy = o.copy();
            JoinKey key = new JoinKey(targets.size(), uid, current.getAndIncrement());
            copy.output(joinKey, key);

            // dispatch new flow for every goTo
            n.forward(copy, target);
        });
    }
}