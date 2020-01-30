package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;

/**
 * Replaces occurrences in a string
 */
@NodePlugin("1.0.0")
public final class StringReplaceNode implements FunctionalNode {

    /** The content to apply the regex on */
    @FlowKey(defaultValue = "\"{content}\"") @NotNull
    private final T<String> content = new T<>(){};

    /** Where the output list will be put. If there's already a list at that key, it will be replaced. */
    @FlowKey(defaultValue = "\"output\"", output = true) @NotNull
    private final T<String> output = new T<>(){};

    /** This string occurrence will be replaced */
    @FlowKey(mandatory = true)
    private String replace;

    /** This is the replacement */
    @FlowKey(mandatory = true)
    private String with;

    @Override
    public void modify(FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.content);

        String newContent = content.replaceAll(replace, with);

        o.output(output, newContent);
    }
}
