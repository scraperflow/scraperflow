package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

/**
 * Replaces occurrences in a string
 */
@NodePlugin("0.1.0")
public final class StringReplaceNode implements FunctionalNode {

    /** All occurrences of this regex will be replaced */
    @FlowKey(mandatory = true)
    private String replace;

    /** This is the replacement for each occurrence */
    @FlowKey(mandatory = true)
    private String with;

    /** The content to apply the regex on */
    @FlowKey(defaultValue = "\"{content}\"")
    private final T<String> content = new T<>(){};

    /** Replaced string output */
    @FlowKey(defaultValue = "\"output\"")
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.content);

        String newContent = content.replaceAll(replace, with);

        o.output(output, newContent);
    }
}
