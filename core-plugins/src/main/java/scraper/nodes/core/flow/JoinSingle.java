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
import scraper.util.TemplateUtil;

import java.util.Map;
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

    /** These keys are joined in a list and put into another key */
    @FlowKey(mandatory = true)
    private final L<List<X>> output = new L<>(){};

    /** Key which can be used to join flows. Needs to match the joinKey used by a previous Fork node. */
    @FlowKey
    private final T<Fork.JoinKey> joinKey = new T<>(){};

    /** Target address to fork to */
    @FlowKey(mandatory = true)
    @Flow(label = "join")
    private Address joinTarget;

    // STATE
    final Map<Fork.JoinKey, List<AbstractMap.SimpleEntry<Fork.JoinKey, X>>> waiting = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        Fork.JoinKey joinKey = o.eval(this.joinKey);

        List<AbstractMap.SimpleEntry<Fork.JoinKey, X>> flows;
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

    private void emit(Collection<AbstractMap.SimpleEntry<Fork.JoinKey, X>> flows, FlowMap evaluator, NodeContainer<? extends Node> n) {
        FlowMap toEmit = evaluator.copy();

        List<X> sortedFlows = flows.stream()
                .sorted(Comparator.comparingInt(joinKeyFlowMapSimpleEntry -> joinKeyFlowMapSimpleEntry.getKey().num))
                .map(AbstractMap.SimpleEntry::getValue)
                .collect(Collectors.toList());


        toEmit.output(output, sortedFlows);
        n.forward(toEmit, joinTarget);
    }
}
