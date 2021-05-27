package scraper.nodes.core.complex;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.annotations.node.Stateful;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;
import scraper.nodes.core.flow.JoinKey;
import scraper.util.TemplateUtil;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Joins on a join key only if all emitted flows belonging to the join key arrive at this join node.
 * Merges multiple keys into lists.
 */
@NodePlugin(value = "0.0.1", customFlowAfter = true)
@Stateful
public final class Join implements Node {

    /** These keys are joined in a list and put into another key */
    @FlowKey(mandatory = true)
    private final T<java.util.Map<String, String>> keys = new T<>(){};

    /** Key which can be used to join flows. Needs to match the joinKey used by a previous Fork node. */
    @FlowKey(mandatory = true)
    private final T<JoinKey> joinKey = new T<>(){};

    // STATE
    final java.util.Map<JoinKey, Collection<SimpleEntry<JoinKey, FlowMap>>> waiting = new ConcurrentHashMap<>();

    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        JoinKey joinKey = o.eval(this.joinKey);

        Collection<SimpleEntry<JoinKey, FlowMap>> flows;
        synchronized (waiting) {
            waiting.computeIfAbsent(joinKey, k -> new HashSet<>());
            flows = waiting.get(joinKey);
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (flows) {
            if (flows.size() < joinKey.size) {
                flows.add(new SimpleEntry<>(joinKey, o.copy()));
            }

            if (flows.size() == joinKey.size) {
                emit(flows, o, n, o.evalIdentity(keys));
            }
        }
    }

    private void emit(Collection<SimpleEntry<JoinKey, FlowMap>> flows, FlowMap evaluator, NodeContainer<? extends Node> n, Map<String, String> keys) {
        FlowMap toEmit = evaluator.copy();

        List<FlowMap> sortedFlows = flows.stream()
                .sorted(Comparator.comparingInt(joinKeyFlowMapSimpleEntry -> joinKeyFlowMapSimpleEntry.getKey().num))
                .map(SimpleEntry::getValue)
                .collect(Collectors.toList());

        keys.forEach((joinKeyd, joinKey) -> {
            List<Object> joinResults = evaluator.evalMaybe(new T<List<Object>>() {}).orElse(new ArrayList<>());

            for (FlowMap flow : sortedFlows) {
                Object forked = flow.eval(TemplateUtil.templateOf(joinKeyd));
                joinResults.add(forked);
            }

            toEmit.output(TemplateUtil.locationOf(joinKey), joinResults);
        });

        n.forward(toEmit);
    }
}
