package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.annotations.Stateful;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.L;
import scraper.api.T;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Joins on a join key.
 */
@NodePlugin(value = "0.0.1")
@Stateful
public final class JoinSingle<X> implements Node {

    /** These keys are joined in a list and put into another key */
    @FlowKey(mandatory = true)
    private final T<X> key = new T<>(){};

    /** Key which can be used to join flows. Needs to match the joinKey used by a previous node. */
    @FlowKey(mandatory = true)
    private final T<JoinKey> joinKey = new T<>(){};

    /** These keys are joined in a list and put into another key */
    @FlowKey(mandatory = true)
    private final L<List<X>> output = new L<>(){};

    // STATE
    final java.util.Map<JoinKey, List<AbstractMap.SimpleEntry<JoinKey, X>>> waiting = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        JoinKey joinKey = o.eval(this.joinKey);

        List<AbstractMap.SimpleEntry<JoinKey, X>> flows;
        synchronized (waiting) {
            waiting.computeIfAbsent(joinKey, k -> new LinkedList<>());
            flows = waiting.get(joinKey);
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (flows) {
            if (flows.size() < joinKey.size) {
                flows.add(new AbstractMap.SimpleEntry<>(joinKey, o.eval(key)));
            }

            if (flows.size() == joinKey.size) {
                emit(flows, o, n);
            }
        }
    }

    private void emit(Collection<AbstractMap.SimpleEntry<JoinKey, X>> flows, FlowMap evaluator, NodeContainer<? extends Node> n) {
        FlowMap toEmit = evaluator.copy();

        List<X> sortedFlows = flows.stream()
                .sorted(Comparator.comparingInt(joinKeyFlowMapSimpleEntry -> joinKeyFlowMapSimpleEntry.getKey().num))
                .map(AbstractMap.SimpleEntry::getValue)
                .collect(Collectors.toList());


        toEmit.output(output, sortedFlows);
        n.forward(toEmit);
    }
}
