package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

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
