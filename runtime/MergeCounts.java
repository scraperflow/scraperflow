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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Merges maps, keys overwritten in order.
 */
@NodePlugin("0.0.1")
public final class MergeCounts implements FunctionalNode {

    /** The maps to merge */
    @FlowKey(mandatory = true)
    private final T<List<Map<String, Integer>>> maps = new T<>(){};

    /** Where the merged map is stored */
    @FlowKey(mandatory = true)
    private final L<Map<String, Integer>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<Map<String, Integer>> merge = o.eval(this.maps);

        Map<String, Integer> merged = merge.stream()
                .flatMap(stringIntegerMap -> stringIntegerMap.entrySet().parallelStream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));

        o.output(output, merged);
    }
}
