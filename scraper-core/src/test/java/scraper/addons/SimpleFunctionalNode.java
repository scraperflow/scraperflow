package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleFunctionalNode implements FunctionalNode {

    @FlowKey
    private String output;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        System.out.println(output);
        if(output != null)
            o.output(output, true);
        else {
            o.output("simple", true);
        }
    }
}