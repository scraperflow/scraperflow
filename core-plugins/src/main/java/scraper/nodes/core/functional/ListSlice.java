package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * Takes a universe and removes all elements in a given list from it.
 */
@NodePlugin("0.2.0")
public final class ListSlice <K> implements FunctionalNode {

    /** List to slice */
    @FlowKey(defaultValue = "[]")
    private final T<List<K>> list = new T<>(){};

    /** */
    @FlowKey(defaultValue = "0")
    private Integer from;

    /** */
    @FlowKey(defaultValue = "0")
    private Integer to;

    /** Sliced list */
    @FlowKey(defaultValue = "\"_\"")
    private final L<List<K>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<K> list = o.eval(this.list);

        int startIndex = from;
        // negative wrap around , stop at zero
        if(from < 0) startIndex = min(0, list.size() - abs(startIndex));

        int endIndex = to;
        // negative wrap around, stop at zero
        if(to < 0) endIndex = min(0, list.size() - abs(endIndex));

        // special case: end index 0
        if(endIndex == 0) endIndex = list.size();

        if(endIndex < startIndex) {
            o.output(output, list);
        } else {
            o.output(output, list.subList(startIndex, endIndex));
        }
    }
}
