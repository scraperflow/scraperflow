package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

import java.util.List;

/**
 * Splits a string
 */
@NodePlugin("0.1.0")
public final class StringSplit implements FunctionalNode {

    /** The content to apply the regex on */
    @FlowKey(mandatory = true)
    private final T<String> content = new T<>(){};

    /** Split */
    @FlowKey(mandatory = true)
    private String regex;

    /** This is the replacement for each occurrence */
    @FlowKey(defaultValue = "0")
    private Integer limit;

    /** Split string output */
    @FlowKey(mandatory = true)
    private final L<List<String>> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.content);
        String[] split = content.split(regex, limit);
        o.output(output, List.of(split));
    }
}
