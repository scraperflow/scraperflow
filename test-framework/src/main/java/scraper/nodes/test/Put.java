package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;

@NodePlugin(value = "1.2.3")
public final class Put implements FunctionalNode {
    @FlowKey
    private final L<String> toPut = new L<>(){};

    @FlowKey(defaultValue = "\"hello\"")
    private final L<String> hello = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        o.output(hello, "world");
        o.output(toPut, "result");
    }
}