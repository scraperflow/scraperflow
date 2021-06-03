package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Merges maps, keys overwritten in order.
 */
@NodePlugin("0.0.1")
public final class MergeMap<A> implements FunctionalNode {

    /** The maps to merge */
    @FlowKey(mandatory = true)
    private final T<List<Map<String, A>>> merge = new T<>(){};

    /** Where the merged map is stored */
    @FlowKey(mandatory = true)
    private final L<Map<String, A>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<Map<String, A>> merge = o.eval(this.merge);
        Map<String, A> kv = new HashMap<>();
        for (Map<String, A> map : merge) { kv.putAll(map); }
        o.output(output, kv);
    }
}
