package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
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
 * Produces a list of maps.
 * The maps capture the content of the capture groups.
 * If groups are empty, the map is empty and the list is populated with empty maps.
 */
@NodePlugin("0.1.0")
public final class StringContainsNode implements FunctionalNode {

    /** Regex as a Java String */
    @FlowKey(mandatory = true)
    private T<String> regex = new T<>(){};

    /** The content to apply the regex on */
    @FlowKey(defaultValue = "\"{content}\"")
    private final T<String> content = new T<>(){};

    /** Where the output list will be put. If there's already a list at that key, it will be replaced. */
    @FlowKey(defaultValue = "\"output\"")
    private L<Boolean> output = new L<>(){};

    /** Pattern dotall option */
    @FlowKey(defaultValue = "\"true\"")
    private Boolean dotAll;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) throws NodeException {
        Pattern p;
        try {
            String regex = o.eval(this.regex);
            if(dotAll) p = Pattern.compile(regex, Pattern.DOTALL);
            else p = Pattern.compile(regex);
        } catch (Exception e) { throw new NodeException(e, "Failed to compile pattern."); }

        String content = o.eval(this.content);

        Matcher m = p.matcher(content);

        if(m.results().findAny().isEmpty()) {
            o.output(output, true);
        } else {
            o.output(output, false);
        }
    }
}
