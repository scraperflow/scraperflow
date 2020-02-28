package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Showcase of possible annotation combinations on fields concerning .scrape definition
 *
 * See the test case for this node.
 */
@NodePlugin(deprecated = true) // only used for tests and showcasing
public class AnnotationsExampleNode implements Node {

    // simple mandatory usage
    @FlowKey(mandatory = true)
    private String mandatoryString;
    // enums are supported, too
    @FlowKey(mandatory = true)
    private SomeEnum mandatoryEnum;

    // T usage
    @FlowKey(mandatory = true) @Argument
    private Integer TInteger;
    @FlowKey(mandatory = true) @Argument
    private Integer TIntegerAsString;
    @FlowKey(mandatory = true) @Argument
    private Integer TIntegerAsStringInt;
    @FlowKey(mandatory = true) @Argument
    private SomeEnum TEnum;

    // mutable T usage
    @FlowKey(mandatory = true)
    private final T<String> simpleT = new T<>(){};
    @FlowKey(mandatory = true)
    private final T<Map<String, Integer>> mapT = new T<>(){};

    // optional
    @FlowKey
    private String optionalNull;
    @FlowKey(defaultValue = "\"base42\"")
    private String optionalBase;
    @FlowKey
    private T<String> optionalNullT = new T<>(){};
    @FlowKey(defaultValue = "\"base42\"")
    private T<String> optionalBaseT = new T<>(){};
    @FlowKey(defaultValue = "\"defaultValue\"")
    private T<String> optionalBaseTEval = new T<>(){};
    @FlowKey(defaultValue = "\"{url}\"")
    private T<String> optionalDefaultT = new T<>(){};
    // T optional complex
    @FlowKey(defaultValue = "{\"key\": \"{url}\"}")
    private T<Map<String, String>> optionalDefaultTComplex = new T<>(){};

    // tests cases where Ts and the actual type are mixed
    @FlowKey(defaultValue = "{\"actual\": 1, \"replaced\": \"{id}\"}")
    private T<Map<String, Integer>> multiT = new T<>(){};

    // optional T
    @FlowKey @Argument
    protected String emptyToNull;

    // output Ts
    // if only the raw type is important
    @FlowKey(mandatory = true)
    private final L<List> outputRaw = new L<>(){};

    @Override @NotNull
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) throws NodeException {
        o.output(outputRaw, new LinkedList());
        return n.forward(o);
    }

    @SuppressWarnings("unused") // used for enum parsing
    enum SomeEnum {
        VALUE_A, VALUE_B
    }
}
