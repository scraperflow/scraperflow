package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

import java.util.List;

/**
 * Flattens a list of strings into a string
 */
@NodePlugin("0.2.0")
public final class StringJoin implements FunctionalNode {

    /** The list with strings to join */
    @FlowKey(mandatory = true)
    private final T<List<String>> list = new T<>(){};

    /** The join delimiter */
    @FlowKey(defaultValue = "\"\"")
    private final T<String> delimiter = new T<>(){};

    /** Where the flattened string is stored */
    @FlowKey(mandatory = true)
    private final L<String> output = new L<>(){};


    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String delimiter = o.eval(this.delimiter);
        String joined = String.join(delimiter, o.eval(this.list));
        o.output(output, joined);
    }
}
