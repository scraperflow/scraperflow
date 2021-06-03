package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.annotations.Stateful;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

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
