package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

/**
 * Replaces occurrences in a string
 */
@NodePlugin("0.3.0")
public final class StringReplace implements FunctionalNode {

    /** The content to apply the regex on */
    @FlowKey(mandatory = true)
    private final T<String> content = new T<>(){};

    /** All occurrences of this regex will be replaced */
    @FlowKey(mandatory = true)
    private String replace;

    /** This is the replacement for each occurrence */
    @FlowKey(mandatory = true)
    private final T<String> with = new T<>(){};

    /** Replaced string output */
    @FlowKey(mandatory = true)
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.content);
        String with = o.eval(this.with);

        String newContent = content.replaceAll(replace, with);

        o.output(output, newContent);
    }
}
