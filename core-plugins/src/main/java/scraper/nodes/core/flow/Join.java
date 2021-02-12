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
import scraper.api.template.T;
import scraper.util.TemplateUtil;

import java.util.Map;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Joins on a join key.
 */
@NodePlugin(value = "0.0.1", deprecated = true, customFlowAfter = true)
@Stateful
public final class Join implements Node {

    /** These keys are joined in a list and put into another key */
    @FlowKey(mandatory = true)
    private final T<java.util.Map<String, String>> keys = new T<>(){};

    /** Key which can be used to join flows. Needs to match the joinKey used by a previous Fork node. */
    @FlowKey
    private final T<Fork.JoinKey> joinKey = new T<>(){};

    /** Target address to fork to */
    @FlowKey(mandatory = true)
    @Flow(dependent = false, crossed = false, label = "join")
    private Address joinTarget;

    // STATE
    final java.util.Map<Fork.JoinKey, Collection<AbstractMap.SimpleEntry<Fork.JoinKey, FlowMap>>> waiting = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        Fork.JoinKey joinKey = o.eval(this.joinKey);

        Collection<AbstractMap.SimpleEntry<Fork.JoinKey, FlowMap>> flows;
        synchronized (waiting) {
            waiting.computeIfAbsent(joinKey, k -> new HashSet<>());
            flows = waiting.get(joinKey);
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (flows) {
            if (flows.size() < joinKey.size) {
                flows.add(new AbstractMap.SimpleEntry<>(joinKey, o.copy()));
            }

            if (flows.size() == joinKey.size) {
                emit(flows, o, n, o.evalIdentity(keys));
            }
        }

        return o;
    }

    private void emit(Collection<AbstractMap.SimpleEntry<Fork.JoinKey, FlowMap>> flows, FlowMap evaluator, NodeContainer<? extends Node> n, Map<String, String> keys) {
        FlowMap toEmit = evaluator.copy();

        List<FlowMap> sortedFlows = flows.stream()
                .sorted(Comparator.comparingInt(joinKeyFlowMapSimpleEntry -> joinKeyFlowMapSimpleEntry.getKey().num))
                .map(AbstractMap.SimpleEntry::getValue)
                .collect(Collectors.toList());

        keys.forEach((joinKeyForked, joinKey) -> {
            List<Object> joinResults = evaluator.evalMaybe(new T<List<Object>>() {}).orElse(new ArrayList<>());

            for (FlowMap flow : sortedFlows) {
                Object forked = flow.eval(TemplateUtil.templateOf(joinKeyForked));
                joinResults.add(forked);
            }

            toEmit.output(TemplateUtil.locationOf(joinKey), joinResults);
        });

        n.forkDispatch(toEmit, joinTarget);
    }
}
