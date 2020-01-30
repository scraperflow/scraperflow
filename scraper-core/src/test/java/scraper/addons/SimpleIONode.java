package scraper.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;

import java.util.List;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleIONode implements FunctionalNode {

    @FlowKey
    private T<List<String>> input = new T<>(){};

    @FlowKey(output = true)
    private T<Integer> output = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        List<String> i = o.eval(input);
        if(i.isEmpty()) {
            o.output(output, -1);
        } else {
            o.output(output, 42);
        }
    }
}