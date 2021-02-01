package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;

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