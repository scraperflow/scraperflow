package scraper.nodes.core.functional;

import scraper.annotations.*;
import scraper.api.*;

/**
 * Checks string equality for two inputs.
 */
@NodePlugin("0.2.0")
public final class StringEqualityCheck implements FunctionalNode {

    /** String 1 */
    @FlowKey(mandatory = true)
    private final T<String> string = new T<>(){};

    /** String 2 */
    @FlowKey(mandatory = true)
    private final T<String> check = new T<>(){};

    /** Result location */
    @FlowKey(mandatory = true)
    private final L<Boolean> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.string);
        String check = o.eval(this.check);
        o.output(output, content.equals(check));
    }
}
