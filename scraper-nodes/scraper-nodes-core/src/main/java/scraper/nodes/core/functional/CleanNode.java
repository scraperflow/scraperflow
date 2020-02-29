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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hashes a values
 */
@NodePlugin("0.1.0")
public final class CleanNode implements FunctionalNode {

    /** What not to remove */
    @FlowKey(defaultValue = "[]") @NotNull
    private T<List<? extends String>> except = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<? extends String> whitelist = o.eval(except);
        Map<String, ? super Object> notRemove = new HashMap<>();
        whitelist.forEach(e -> o.get(e).ifPresent(val -> notRemove.put(e, val)));
        o.clear();
        notRemove.forEach(o::output);
    }
}
