package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.annotations.node.Stateful;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Joins on a join key, only flow.
 * Can be used instead of Join to save state memory.
 */
@NodePlugin(value = "0.0.1")
@Stateful
public final class JoinFlow implements Node {

    /** Key which can be used to join flows. Needs to match the joinKey used by a previous node. */
    @FlowKey(mandatory = true)
    private final T<JoinKey> joinKey = new T<>(){};

    // STATE
    final java.util.Map<JoinKey, Integer> waiting = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        JoinKey joinKey = o.eval(this.joinKey);

        Integer flows;
        synchronized (waiting) {
            waiting.putIfAbsent(joinKey, 0);
            flows = waiting.get(joinKey);
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (flows) {
            if (flows < joinKey.size) {
                flows += 1;
                waiting.put(joinKey, flows);
            }

            if (flows == joinKey.size) {
                n.forward(o);
            }
        }
    }
}
