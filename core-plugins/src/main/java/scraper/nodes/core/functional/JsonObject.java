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
 * Adds possibility to create a big complex object with a not allowed type,
 * can be used to construct JSON objects as output if you don't refer to it as input later.
 * Be careful.
 */
@NodePlugin("0.0.1")
public class JsonObject implements FunctionalNode {

    /** Element to output */
    @FlowKey(mandatory = true)
    private final T<Object> value = new T<>(){};

    /** Location of output */
    @FlowKey(mandatory = true)
    private final L<Object> put = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Object value = o.eval(this.value);
        o.output(put, value);
    }
}
