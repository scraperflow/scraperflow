package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

/**
 * String to Integer conversion.
 * Throws an exception on invalid Integers.
 */
@NodePlugin("0.0.1")
public class StringToInt implements FunctionalNode {

    /** String input */
    @FlowKey(mandatory = true)
    private final T<String> string = new T<>(){};

    /** Integer output */
    @FlowKey(mandatory = true)
    private final L<Integer> integer = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String value = o.eval(this.string);
        o.output(integer, Integer.parseInt(value));
    }
}
