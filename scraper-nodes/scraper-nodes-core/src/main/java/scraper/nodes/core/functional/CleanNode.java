package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.T;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cleans the current flow map.
 */
@NodePlugin("0.2.0")
public final class CleanNode implements FunctionalNode {

    /** Whitelist, what keys not to remove */
    @FlowKey(defaultValue = "[]")
    private T<List<String>> except = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        List<String> whitelist = o.eval(except);
        Map<String, ? super Object> notRemove = new HashMap<>();
        whitelist.forEach(e -> o.get(e).ifPresent(val -> notRemove.put(e, val)));
        o.clear();
        notRemove.forEach(o::output);
    }
}
