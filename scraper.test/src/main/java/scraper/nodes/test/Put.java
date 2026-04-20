package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;

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