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
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Forks to targets and continues the flow.
 * Support for joins by joinKey, if supplied.
 */
@NodePlugin("0.3.0")
public final class Fork implements Node {

    /** All nodes to fork the current flow map to */
    @FlowKey(mandatory = true)
    @Flow(dependent = false, crossed = false, label = "fork")
    private final T<List<Address>> forkTargets = new T<>(){};

    /** Key which can be used to join flows */
    @FlowKey(defaultValue = "\"_\"")
    private final L<JoinKey> joinKey = new L<>(){};

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        List<Address> targets = o.evalIdentity(forkTargets);
        int uid = new Random().nextInt();
        AtomicInteger current = new AtomicInteger();
        o.evalIdentity(forkTargets).forEach(target -> {
            FlowMap copy = o.copy();
            JoinKey key = new JoinKey(targets.size(), uid, current.getAndIncrement());
            copy.output(joinKey, key);

            // dispatch new flow for every goTo
            n.forkDispatch(copy, target);
        });

        return o;
    }

    static class JoinKey {
        final int size;
        final int num;
        final int uid;
        public JoinKey(int size, int uid, int num) { this.size = size; this.uid = uid; this.num = num; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JoinKey joinKey = (JoinKey) o;
            return uid == joinKey.uid;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uid);
        }
    }
}
