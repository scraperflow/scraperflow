package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

/**
 * Replaces occurrences in a string
 */
@NodePlugin("1.0.0")
public final class StringReplaceNode extends AbstractFunctionalNode {

    /** The content to apply the regex on */
    @FlowKey(defaultValue = "\"{content}\"") @NotNull
    private final Template<String> content = new Template<>(){};

    /** Where the output list will be put. If there's already a list at that key, it will be replaced. */
    @FlowKey(defaultValue = "\"output\"", output = true) @NotNull
    private final Template<String> output = new Template<>(){};

    /** This string occurrence will be replaced */
    @FlowKey(mandatory = true)
    private String replace;

    /** This is the replacement */
    @FlowKey(mandatory = true)
    private String with;

    @Override
    public void modify(@NotNull final FlowMap o) {
        String content = this.content.eval(o);

        String newContent = content.replaceAll(replace, with);

        output.output(o, newContent);
    }
}
