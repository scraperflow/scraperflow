package scraper.nodes.core.flow;


import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.L;
import scraper.api.T;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Emits new flows for every element in <var>list</var>.
 * The element is saved to <var>putElement</var>.
 * Can be joined by joinKey.
 */
@NodePlugin(value = "0.6.0")
public final class Map <K> implements Node {

    /** The expected list is located to fork on */
    @FlowKey(mandatory = true)
    private final T<List<K>> list = new T<>(){};

    /** At which key to put the element of the list into. */
    @FlowKey(mandatory = true)
    private final L<K> putElement = new L<>(){};

    /** Key which can be used to join flows */
    @FlowKey(defaultValue = "\"_\"")
    private final L<JoinKey> joinKey = new L<>(){};

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        List<K> targetList = o.eval(list);
        int uid = new Random().nextInt();
        AtomicInteger current = new AtomicInteger();
        targetList.forEach(t -> {
            FlowMap finalCopy = o.copy();
            finalCopy.output(putElement, t);

            JoinKey key = new JoinKey(targetList.size(), uid, current.getAndIncrement());
            finalCopy.output(joinKey, key);
            n.forward(finalCopy);
        });
    }
}
