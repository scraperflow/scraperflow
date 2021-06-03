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
 * Hashes a value of any content
 */
@NodePlugin("0.5.0")
public final class Hash <A> implements FunctionalNode {

    /** The content to apply the hash on */
    @FlowKey(mandatory = true)
    private final T<A> content = new T<>(){};

    /** Where the output hash is stored */
    @FlowKey(mandatory = true)
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        Object content = o.eval(this.content);
        int hash = content.hashCode();
        o.output(output, String.valueOf(hash));
    }
}
