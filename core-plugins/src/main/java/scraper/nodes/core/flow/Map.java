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
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Forks new flows for every element in <var>list</var>.
 * Does not wait or join the forked flows.
 * The element is saved to <var>putElement</var>
 */
@NodePlugin(value = "0.5.0")
public final class Map <K> implements Node {

    /** The expected list is located to fork on */
    @FlowKey
    private final T<List<K>> list = new T<>(){};

    /** The expected map is located to fork on */
    @FlowKey
    private final T<java.util.Map<String, K>> map = new T<>(){};

    /** Target address to fork to */
    @FlowKey(mandatory = true)
    @Flow(label = "map")
    private Address mapTarget;

    /** At which key to put the element of the list into. */
    @FlowKey(defaultValue = "\"element\"")
    private final L<K> putElement = new L<>(){};

    /** At which key to put the element of the map key if any into. */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> putElementKey = new L<>(){};

    /** Key which can be used to join flows */
    @FlowKey(defaultValue = "\"_\"")
    private final L<Fork.JoinKey> joinKey = new L<>(){};

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        {
            Optional<List<K>> targetList = o.evalMaybe(list);
            int uid = new Random().nextInt();
            AtomicInteger current = new AtomicInteger();
            targetList.ifPresent(ks -> ks.forEach(t -> {
                FlowMap finalCopy = o.copy();
                finalCopy.output(putElement, t);

                Fork.JoinKey key = new Fork.JoinKey(ks.size(), uid, current.getAndIncrement());
                finalCopy.output(joinKey, key);
                n.forward(finalCopy, mapTarget);
            }));

        }

        {
            int uid = new Random().nextInt();
            AtomicInteger current = new AtomicInteger();
            Optional<java.util.Map<String, K>> targetMap = o.evalMaybe(map);
            targetMap.ifPresent(stringKMap -> stringKMap.forEach((k, v) -> {
                FlowMap finalCopy = o.copy();
                finalCopy.output(putElement, v);
                finalCopy.output(putElementKey, k);

                Fork.JoinKey key = new Fork.JoinKey(stringKMap.size(), uid, current.getAndIncrement());
                finalCopy.output(joinKey, key);
                n.forward(finalCopy, mapTarget);
            }));

        }
    }
}
