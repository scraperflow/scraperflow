package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.node.*;
import scraper.api.flow.FlowMap;
import scraper.core.*;
import scraper.api.exceptions.NodeException;

import java.util.*;


/**
 * Showcase of possible annotation combinations on fields concerning .scrape definition
 *
 * See the test case for this node.
 */
@NodePlugin(deprecated = true) // only used for tests and showcasing
public class AnnotationsExampleNode extends AbstractNode {

    // simple mandatory usage
    @FlowKey(mandatory = true)
    private String mandatoryString;
    // enums are supported, too
    @FlowKey(mandatory = true)
    private SomeEnum mandatoryEnum;

    // template usage
    @FlowKey(mandatory = true) @Argument
    private Integer templateInteger;
    @FlowKey(mandatory = true) @Argument
    private Integer templateIntegerAsString;
    @FlowKey(mandatory = true) @Argument
    private Integer templateIntegerAsStringInt;
    @FlowKey(mandatory = true) @Argument
    private SomeEnum templateEnum;

    // mutable template usage
    @FlowKey(mandatory = true)
    private final Template<String> simpleTemplate = new Template<>(){};
    @FlowKey(mandatory = true)
    private final Template<Map<String, Integer>> mapTemplate = new Template<>(){};

    // optional
    @FlowKey
    private String optionalNull;
    @FlowKey(defaultValue = "\"base42\"")
    private String optionalBase;
    @FlowKey
    private Template<String> optionalNullTemplate = new Template<>(){};
    @FlowKey(defaultValue = "\"base42\"")
    private Template<String> optionalBaseTemplate = new Template<>(){};
    @FlowKey(defaultValue = "\"defaultValue\"")
    private Template<String> optionalBaseTemplateEval = new Template<>(){};
    @FlowKey(defaultValue = "\"{url}\"")
    private Template<String> optionalDefaultTemplate = new Template<>(){};
    // template optional complex
    @FlowKey(defaultValue = "{\"key\": \"{url}\"}")
    private Template<Map<String, String>> optionalDefaultTemplateComplex = new Template<>(){};

    // tests cases where templates and the actual type are mixed
    @FlowKey(defaultValue = "{\"actual\": 1, \"replaced\": \"{id}\"}")
    private Template<Map<String, Integer>> multiTemplate = new Template<>(){};

    // optional template
    @FlowKey @Argument
    protected String emptyToNull;

    // output templates
    // if only the raw type is important
    @FlowKey(mandatory = true, output = true)
    private final Template<List> outputRaw = new Template<>(){};

    @Override @NotNull
    public FlowMap process(@NotNull final FlowMap o) throws NodeException {
        outputRaw.output(o, new LinkedList());
        return forward(o);
    }

    enum SomeEnum {
        VALUE_A, VALUE_B
    }
}
