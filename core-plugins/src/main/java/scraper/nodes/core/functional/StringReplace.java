package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

/**
 * Replaces occurrences in a string
 */
@NodePlugin("0.2.0")
public final class StringReplace implements FunctionalNode {

    /** All occurrences of this regex will be replaced */
    @FlowKey(mandatory = true)
    private String replace;

    /** This is the replacement for each occurrence */
    @FlowKey(mandatory = true)
    private final T<String> with = new T<>(){};

    /** The content to apply the regex on */
    @FlowKey(defaultValue = "\"{content}\"")
    private final T<String> content = new T<>(){};

    /** Replaced string output */
    @FlowKey(defaultValue = "\"output\"")
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.content);
        String with = o.eval(this.with);

        String newContent = content.replaceAll(replace, with);

        o.output(output, newContent);
    }
}
