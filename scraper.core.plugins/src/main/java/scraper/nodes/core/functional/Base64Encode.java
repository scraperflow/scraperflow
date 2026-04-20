package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

import java.util.Base64;

/**
 * Encode a string into Base64
 */
@NodePlugin("0.2.0")
public final class Base64Encode implements FunctionalNode {

    /** String to encode */
    @FlowKey(mandatory = true)
    private final T<String> encode = new T<>(){};

    /** Encoded Base64 String */
    @FlowKey(mandatory = true)
    private final L<String> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        o.output(output, Base64.getUrlEncoder().encodeToString(o.eval(encode).getBytes()));
    }
}
