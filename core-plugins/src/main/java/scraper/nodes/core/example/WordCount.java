package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.annotations.node.Stateful;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * stateless word count
 */
@NodePlugin(value = "0.0.1")
@Stateful
public class WordCount implements FunctionalNode {

    @FlowKey
    private final L<Map<String, Integer>> put = new L<>(){};

    @FlowKey
    private final T<String> line = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        StringTokenizer tokenizer = new StringTokenizer(o.eval(line), " ");
        Map<String, Integer> maps = new HashMap<>();
        try {
            while(true) {
                String token = tokenizer.nextToken();
                maps.merge(token, 1, Integer::sum);
            }
        } catch (Exception e) {}

        o.output(put, maps);
    }
}
