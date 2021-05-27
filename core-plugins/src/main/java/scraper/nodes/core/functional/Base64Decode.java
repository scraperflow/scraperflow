package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Base64;

/**
 * Decode a Base64 string into a String.
 * Will crash the flow if the String is not a valid Base64 String
 */
@NodePlugin("0.2.0")
public final class Base64Decode implements FunctionalNode {

    /** Decode string */
    @FlowKey(mandatory = true)
    private final T<String> decode = new T<>(){};

    /** Where the output will be */
    @FlowKey(mandatory = true)
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        o.output(output, new String(Base64.getUrlDecoder().decode(o.eval(decode))));
    }
}
