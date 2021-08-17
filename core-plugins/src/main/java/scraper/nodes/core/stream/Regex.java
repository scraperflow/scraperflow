package scraper.nodes.core.stream;

import scraper.annotations.NotNull;
import scraper.annotations.Argument;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeIOException;
import scraper.api.ValidationException;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.StreamNodeContainer;
import scraper.api.Node;
import scraper.api.StreamNode;
import scraper.api.ScrapeInstance;
import scraper.api.L;
import scraper.api.T;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies a regex to some input content.
 *
 * Produces a list of maps.
 * The maps capture the content of the capture groups.
 * If groups are empty, the map is empty and the list is populated with empty maps.
 * <p>
 *     The String needs to be properly escaped in the specification.
 * </p>
 * Example:
 * <pre>
 * type: Regex
 * regex: "((.*?\\/)(\\w*)\\.java)"
 * groups:
 *   path: 1
 *   folder: 2
 *   classname: 3
 * content: "{filename}"
 * output: fileinfo
 * </pre>
 */
@NodePlugin("0.16.0")
public final class Regex implements StreamNode {

    /** Regex as a (properly escaped JSON) Java String */
    @FlowKey(mandatory = true) @Argument
    private String regex;

    /** The content to apply the regex on */
    @FlowKey(mandatory = true)
    private final T<String> content = new T<>(){};

    /** Key: location of where to put the group; Value: Group number of the regex. */
    @FlowKey(defaultValue = "{}")
    private final T<Map<String, Integer>> groups = new T<>(){};

    /** Default output if no matches are present */
    @FlowKey
    private final T<Map<String, String>> noMatchDefaultOutput = new T<>(){};

    /** Non-empty match expected */
    @FlowKey(defaultValue = "false")
    private Boolean nonempty;

    /** Timeout for execution in ms */
    @FlowKey(defaultValue = "2000")
    private Integer timeout;

    /**
     * Pattern dotall option.
     * <p>In this mode the expression <code>.</code> matches any character,
     * including a line terminator.
     * By default, this expression does not match line terminators.
     */
    @FlowKey(defaultValue = "\"true\"")
    private Boolean dotAll;

    /** Where the output list will be put. If there's already a list at that key, it will be replaced. */
    @FlowKey(mandatory = true)
    private final L<Map<String, String>> output = new L<>(){};

    // compiles the regex pattern
    private Pattern p;

    @Override
    public void init(@NotNull NodeContainer<? extends Node> n, final @NotNull ScrapeInstance job) throws ValidationException {
        try {
            if(dotAll) p = Pattern.compile(regex, Pattern.DOTALL);
            else p = Pattern.compile(regex);
        } catch (Exception e) {
            throw new ValidationException(e, "Failed to compile pattern."); }
    }

    @Override
    public void process(@NotNull final StreamNodeContainer n, @NotNull final FlowMap o) {
        String content = o.eval(this.content);
        Map<String, Integer> groups = o.evalIdentity(this.groups);

        Matcher m = createMatcherWithTimeout(content, p, timeout);

        boolean atLeastOne = false;

        // match regex until no matches found
        while (m.find()) {
            atLeastOne = true;
            Map<String, String> singleCapture = new HashMap<>(groups.keySet().size());
            for (String name : groups.keySet()) {
                Integer group = groups.get(name);
                singleCapture.put(name, m.group(group));
            }

            FlowMap copy = o.copy();
            copy.output(output, singleCapture);
            n.streamFlowMap(o, copy);
        }


        Optional<Map<String, String>> evalDefault = o.evalMaybe(noMatchDefaultOutput);

        if(nonempty && !atLeastOne && evalDefault.isEmpty()) {
            throw new NodeIOException("No match for this regex");
        }

        if(evalDefault.isPresent() && m.reset().results().findAny().isEmpty()) {
            FlowMap copy = o.copy();
            copy.output(output, evalDefault.get());
            n.streamFlowMap(o, copy);
        }
    }




    private static Matcher createMatcherWithTimeout(String stringToMatch, String regularExpression, int timeoutMillis) {
        Pattern pattern = Pattern.compile(regularExpression);
        return createMatcherWithTimeout(stringToMatch, pattern, timeoutMillis);
    }

    private static Matcher createMatcherWithTimeout(String stringToMatch, Pattern regularExpressionPattern, int timeoutMillis) {
        CharSequence charSequence = new TimeoutRegexCharSequence(stringToMatch, timeoutMillis, stringToMatch,
                regularExpressionPattern.pattern());
        return regularExpressionPattern.matcher(charSequence);
    }

    private static class TimeoutRegexCharSequence implements CharSequence {

        private final CharSequence inner;

        private final int timeoutMillis;

        private final long timeoutTime;

        private final String stringToMatch;

        private final String regularExpression;

        public TimeoutRegexCharSequence(CharSequence inner, int timeoutMillis, String stringToMatch, String regularExpression) {
            super();
            this.inner = inner;
            this.timeoutMillis = timeoutMillis;
            this.stringToMatch = stringToMatch;
            this.regularExpression = regularExpression;
            timeoutTime = System.currentTimeMillis() + timeoutMillis;
        }

        public char charAt(int index) {
            if (System.currentTimeMillis() > timeoutTime) {
                throw new RuntimeException("Timeout occurred after " + timeoutMillis + "ms while processing regular expression '"
                        + regularExpression + "' on input '" + stringToMatch + "'!");
            }
            return inner.charAt(index);
        }

        public int length() {
            return inner.length();
        }

        public CharSequence subSequence(int start, int end) {
            return new TimeoutRegexCharSequence(inner.subSequence(start, end), timeoutMillis, stringToMatch, regularExpression);
        }

        @Override
        public String toString() {
            return inner.toString();
        }
    }
}
