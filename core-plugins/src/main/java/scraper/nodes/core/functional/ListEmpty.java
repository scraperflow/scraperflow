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

/**
 * Checks if a list is empty.
 */
@NodePlugin("0.1.0")
public final class ListEmpty <K> implements FunctionalNode {

    /** List to check the size */
    @FlowKey(defaultValue = "[]")
    private final T<List<K>> list = new T<>(){};

    /** Where the boolean result is stored */
    @FlowKey(defaultValue = "\"_\"")
    private final L<Boolean> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.output(put, o.eval(list).isEmpty());
    }
}
