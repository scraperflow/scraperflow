package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;

/**
 * Hashes a values
 */
@NodePlugin("1.0.0")
public final class HashNode implements FunctionalNode {

    /** The content to apply the hash on */
    @FlowKey(defaultValue = "\"{input}\"") @NotNull
    private final T<String> content = new T<>(){};

    /** Where the output hash is stored */
    @FlowKey(defaultValue = "\"output\"", output = true) @NotNull
    private T<String> output = new T<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.content);

        int hash = content.hashCode();

        o.output(output, String.valueOf(hash));
    }
}
