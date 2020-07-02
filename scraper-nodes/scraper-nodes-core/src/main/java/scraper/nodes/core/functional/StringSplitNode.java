package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.List;

/**
 * Splits a string
 */
@NodePlugin("0.1.0")
public final class StringSplitNode implements FunctionalNode {

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
