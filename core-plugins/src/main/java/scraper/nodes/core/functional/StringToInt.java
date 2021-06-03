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
        // put object
        o.output(integer, Integer.parseInt(value));
    }
}
