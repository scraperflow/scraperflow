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
