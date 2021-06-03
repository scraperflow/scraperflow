package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.util.List;

/**
 * Splits a string
 */
@NodePlugin("0.1.0")
public final class StringSplit implements FunctionalNode {

    /** Split */
    @FlowKey(mandatory = true)
    private String regex;

    /** This is the replacement for each occurrence */
    @FlowKey(defaultValue = "0")
    private Integer limit;

    /** The content to apply the regex on */
    @FlowKey(defaultValue = "\"{content}\"")
    private final T<String> content = new T<>(){};

    /** Split string output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<List<String>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.content);
        String[] split = content.split(regex, limit);
        o.output(output, List.of(split));
    }
}
