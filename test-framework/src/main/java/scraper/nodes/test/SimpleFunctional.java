package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;

@NodePlugin(value = "0.2.0", deprecated = true)
public final class SimpleFunctional implements FunctionalNode {

    @FlowKey(defaultValue = "\"simple\"")
    private final L<Boolean> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        System.out.println(output);
        o.output(output, true);
    }
}