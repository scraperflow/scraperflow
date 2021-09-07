package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.util.List;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleIO implements FunctionalNode {

    @FlowKey
    private T<List<String>> input = new T<>(){};

    @FlowKey
    private L<Integer> output = new L<>(){};

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