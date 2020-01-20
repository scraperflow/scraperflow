package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

import java.util.List;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleIONode extends AbstractFunctionalNode {

    @FlowKey
    private Template<List<String>> input = new Template<>(){};

    @FlowKey(output = true)
    private Template<Integer> output = new Template<>(){};

    @Override
    public void modify(@NotNull final FlowMap o) {
        List<String> i = input.eval(o);
        if(i.isEmpty()) {
            output.output(o, -1);
        } else {
            output.output(o, 42);
        }
    }
}