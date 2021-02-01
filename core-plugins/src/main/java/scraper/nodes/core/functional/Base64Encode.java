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
 * Encode a string into Base64
 */
@NodePlugin("0.1.0")
public final class Base64Encode implements FunctionalNode {

    /** String to encode */
    @FlowKey(mandatory = true)
    private final T<String> encode = new T<>(){};

    /** Encoded Base64 String */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        o.output(output, Base64.getUrlEncoder().encodeToString(o.eval(encode).getBytes()));
    }
}
