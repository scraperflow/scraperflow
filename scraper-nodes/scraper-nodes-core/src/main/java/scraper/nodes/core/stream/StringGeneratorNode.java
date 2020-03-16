package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.StreamNodeContainer;
import scraper.api.node.type.StreamNode;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Generate strings based on a generator string and an expression which specifies a range to be generated.
 * </p>
 *
 * <p>
 * Currently only one key expression is supported.
 * </p>
 *
 * <p>
 * Example:
 *
 * <pre>
 *  generator: "page={range}"
 *  expression: "range 2 TO 10"
 * </pre>
 *
 * will generate a list/will stream the following Strings
 * <pre>
 *     "page=2"
 *     ...
 *     "page=10"
 * </pre>
 * with generated strings and the expression used and the current value.
 * </p>
 */
@NodePlugin("0.10.0")
public final class StringGeneratorNode implements StreamNode {

    /** String used to generate a list of more strings. Should contain the KEY part of <var>expression</var>. */
    @FlowKey(mandatory = true)
    private final T<String> generator = new T<>(){};

    /** Expression currently supported: "KEY X TO Y", where X and Y are Integers. */
    @FlowKey(mandatory = true)
    private String expression;

    /** Where the generated String is stored */
    @FlowKey(defaultValue = "\"generated\"")
    private L<String> generatedElement = new L<>(){};

    @Override
    public void process(@NotNull StreamNodeContainer n, @NotNull FlowMap o) {
        // parse expression for goTo key
        // only one pattern 'X TO Y' supported, parse directly with regex
        String targetString = expression;
        Pattern p = Pattern.compile("(\\w*)\\s(\\d*) TO (\\d*)", Pattern.DOTALL);
        Matcher m = p.matcher(targetString);

        if(!m.find()) throw new UnsupportedOperationException("Other expressions than 'KEY X TO Y' are not supported: " + targetString);

        String key = String.valueOf(m.group(1));
        int from = Integer.parseInt(m.group(2));
        int to = Integer.parseInt(m.group(3));

        n.collect(o, List.of(String.valueOf(generatedElement.getTerm().getRaw()), key));

        for (int i = from; i <= to; i++) {
            FlowMap copy = o.copy();
            // used to evaluate generator T
            copy.output(key, i);

            String generatedString = copy.eval(generator);
            copy.output(generatedElement, generatedString);

            n.streamFlowMap(o, copy);
        }
    }
}
