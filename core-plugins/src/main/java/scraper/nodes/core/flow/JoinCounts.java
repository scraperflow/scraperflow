package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.Flow;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.annotations.node.Stateful;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Map;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Joins on a join key.
 */
@NodePlugin(value = "0.0.1")
@Stateful
public final class JoinCounts implements Node {

    /** Word count map to join */
    @FlowKey(mandatory = true)
    private final T<Map<String, Integer>> map = new T<>(){};

    /** Key which can be used to join flows. */
    @FlowKey
    private final T<Fork.JoinKey> joinKey = new T<>(){};

    /** Once all maps are joined the output count map is emitted */
    @FlowKey(mandatory = true)
    private final L<Map<String, Integer>> output = new L<>(){};

    /** Target address to fork to */
    @FlowKey(mandatory = true)
    @Flow(label = "join")
    private Address joinTarget;

    // STATE
    final Lock l = new ReentrantLock();
    final Map<String, Integer> allcounts = new HashMap<>();
    Integer joins = 0;

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        Fork.JoinKey joinKey = o.eval(this.joinKey);
        Map<String, Integer> counts = o.eval(map);

        l.lock();
        try {
            joins++;
            for (String key : counts.keySet()) {
                allcounts.merge(key, counts.get(key), Integer::sum);
            }

            if(joins == joinKey.size) {
                emit(o, n);
            }
        } finally {
            l.unlock();
        }
    }

    private void emit(FlowMap evaluator, NodeContainer<? extends Node> n) {
        FlowMap toEmit = evaluator.copy();
        toEmit.output(output, allcounts);
        n.forward(toEmit, joinTarget);
    }
}
