package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeIOException;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;
import scraper.api.T;

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
