package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.specification.ScrapeInstance;
import scraper.core.AbstractStreamNode;
import scraper.core.Template;
import scraper.util.NodeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies a regex to some input content.
 *
 * Produces a list of maps.
 * The maps capture the content of the capture groups.
 * If groups are empty, the map is empty and the list is populated with empty maps.
 */
@NodePlugin("1.0.0")
public final class RegexNode extends AbstractStreamNode {

    /** Regex as a Java String */
    @FlowKey(mandatory = true) @NotNull
    private String regex;

    /** The content to apply the regex on */
    @FlowKey(defaultValue = "\"{content}\"") @NotNull
    private final Template<String> content = new Template<>(){};

    /** Key: location of where to put the group; Value: Group number of the regex. */
    @FlowKey(defaultValue = "{}") @NotNull
    private Map<String, Integer> groups;

    /** Where the output list will be put. If there's already a list at that key, it will be replaced. */
    @FlowKey(defaultValue = "\"output\"") @NotNull
    private String output;

    /** Pattern dotall option */
    @FlowKey(defaultValue = "\"true\"") @NotNull
    private Boolean dotAll;

    // compiles the regex pattern
    private @NotNull Pattern p;

    @Override
    public void init(final @NotNull ScrapeInstance job) throws ValidationException {
        super.init(job);
        if(dotAll) {
            p = Pattern.compile(regex, Pattern.DOTALL);
        } else {
            p = Pattern.compile(regex);
        }
    }

    @Override
    public FlowMap process(final @NotNull FlowMap o) throws NodeException {
        String content = this.content.eval(o);

        Matcher m = p.matcher(content);

        // match regex until no matches found
        while (m.find()) {
            Map<String, String> singleCapture = new HashMap<>(groups.keySet().size());
            for (String name : groups.keySet()) {
                Integer group = groups.get(name);
                singleCapture.put(name, m.group(group));
            }

            FlowMap copy = NodeUtil.flowOf(o);
            copy.put(output, singleCapture);
            stream(o, copy, List.of(output));
        }

        return forward(o);
    }
}
