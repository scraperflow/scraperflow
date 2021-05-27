package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeIOException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies a regex to some input content.
 *
 * Returns true or false depending on the regex matching the input or not.
 */
@NodePlugin("0.3.0")
public final class StringContains implements FunctionalNode {

    /** Regex as a (properly escaped) Java String */
    @FlowKey(mandatory = true)
    private final T<String> regex = new T<>(){};

    /** The content to apply the regex on */
    @FlowKey(mandatory = true)
    private final T<String> content = new T<>(){};

    /** Where the output will be */
    @FlowKey(mandatory = true)
    private final L<Boolean> output = new L<>(){};

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {
        Pattern p;
        try {
            String regex = o.eval(this.regex);
            p = Pattern.compile(regex);
        } catch (Exception e) { throw new NodeIOException(e, "Failed to compile pattern."); }

        String content = o.eval(this.content);

        Matcher m = p.matcher(content);

        o.output(output, m.results().findAny().isPresent());
    }
}
