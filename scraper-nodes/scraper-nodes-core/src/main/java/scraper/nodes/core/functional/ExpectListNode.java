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
import java.util.Optional;

@NodePlugin("2.0.0")
public class ExpectListNode implements FunctionalNode {

    /** Element to output */
    @FlowKey
    private final T<List<String>> key = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.eval(key);
    }
}
