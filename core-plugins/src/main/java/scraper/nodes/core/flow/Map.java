package scraper.nodes.core.flow;

import scraper.annotations.*;
import scraper.api.*;

import java.util.List;
import java.util.Random;


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
        int current = 0;
        for (K e : targetList) {
            FlowMap finalCopy = o.copy();
            finalCopy.output(putElement, e);

            JoinKey key = new JoinKey(targetList.size(), uid, current++);
            finalCopy.output(joinKey, key);
            n.forward(finalCopy);
        }
    }
}
