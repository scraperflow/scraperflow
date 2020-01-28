package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractStreamNode;
import scraper.core.Template;
import scraper.util.NodeUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate strings based on a generator string and a map which specifies expressions to apply to template parts.
 *
 * Currently only one key expression is supported. A map with size greater than 1 will throw a RuntimeException.
 *
 * Currently, only the 'TO' expression is implemented.
 *
 * Example:
 *
 *  generator: "page={range}"
 *
 *  keysToExpressions: {"range": "2 TO 300"}
 *
 * will generate a list [{"generated": "page=2", "range": "2"},  ... ,{"generated": "page=300", "range": "300"}]
 * with generated strings and the expression used and the current value.
 *
 * @author Albert Schimpf
 */
@NodePlugin("1.0.0")
public final class StringGeneratorNode extends AbstractStreamNode {

    /** String used to generate a list of more strings */
    @FlowKey(mandatory = true) @NotNull
    private final Template<String> generator = new Template<>(){};

    /** Target key in template to (String) expression map.
     * Expression currently supported: "KEY X TO Y", where X and Y are Integers.
     */
    @FlowKey(mandatory = true)
    private String expression;

    /** Output list key */
    @FlowKey(defaultValue = "\"generated\"", output = true) @NotNull
    private Template<String> generatedElement = new Template<>(){};

    @NotNull
    @Override
    public void processStream(final @NotNull FlowMap o) {
        // parse expression for goTo key
        // only one pattern 'X TO Y' supported, parse directly with regex
        String targetString = expression;
        Pattern p = Pattern.compile("(\\w*)\\s(\\d*) TO (\\d*)", Pattern.DOTALL);
        Matcher m = p.matcher(targetString);

        if(!m.find()) throw new UnsupportedOperationException("Other expressions than 'KEY X TO Y' are not supported: " + targetString);

        String key = String.valueOf(m.group(1));
        Integer from = Integer.valueOf(m.group(2));
        Integer to = Integer.valueOf(m.group(3));

        for (int i = from; i <= to; i++) {
            FlowMap copy = NodeUtil.flowOf(o);
            // used to evaluate generator template
            copy.put(key, i);

            String generatedString = generator.eval(copy);
            generatedElement.output(copy, generatedString);

            stream(o, copy, List.of(generatedElement.raw(), key));
        }
    }
}
