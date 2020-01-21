package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

/**
 * Hashes a values
 */
@NodePlugin("1.0.0")
public final class HashNode extends AbstractFunctionalNode {

    /** The content to apply the hash on */
    @FlowKey(defaultValue = "\"{input}\"") @NotNull
    private final Template<String> content = new Template<>(){};

    /** Where the output hash is stored */
    @FlowKey(defaultValue = "\"output\"", output = true) @NotNull
    private Template<String> output = new Template<>(){};

    @Override
    public void modify(@NotNull final FlowMap o) {
        String content = this.content.input(o);

        int hash = content.hashCode();

        this.output.output(o, String.valueOf(hash));
    }
}
