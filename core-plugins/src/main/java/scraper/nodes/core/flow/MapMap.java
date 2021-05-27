package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Forks new flows for every element in <var>map</var>.
 * Does not wait or join the forked flows.
 * The element is saved to <var>putElement</var>
 */
@NodePlugin(value = "0.6.0")
public final class MapMap<K> implements Node {

    /** The expected map is located to fork on */
    @FlowKey(mandatory = true)
    private final T<java.util.Map<String, K>> map = new T<>(){};

    /** At which key to put the element of the list into. */
    @FlowKey(mandatory = true)
    private final L<K> putElement = new L<>(){};

    /** At which key to put the element of the map key if any into. */
    @FlowKey(defaultValue = "_")
    private final L<String> putElementKey = new L<>(){};

    /** Key which can be used to join flows */
    @FlowKey(defaultValue = "\"_\"")
    private final L<JoinKey> joinKey = new L<>(){};

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        int uid = new Random().nextInt();
        AtomicInteger current = new AtomicInteger();
        java.util.Map<String, K> targetMap = o.eval(map);
        targetMap.forEach((k, v) -> {
            FlowMap finalCopy = o.copy();
            finalCopy.output(putElement, v);
            finalCopy.output(putElementKey, k);

            JoinKey key = new JoinKey(targetMap.size(), uid, current.getAndIncrement());
            finalCopy.output(joinKey, key);
            n.forward(finalCopy);
        });
    }
}
