package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.Map;


/**
 * Showcase of all possible annotation combinations on configuration fields
 */
@NodePlugin(value = "0.17.0") // only used for tests and showcasing
public class AnnotationsExample implements Node {

    /**
     * This configuration is mandatory and has to be defined in the configuration.
     * <p>
     * The key in the configuration has to be a <code>JSON String</code>
     */
    @FlowKey(mandatory = true)
    private String mandatoryString;
    /** Java Enums are supported, too. The String needs to be convertible to the target Enum. */
    @FlowKey(mandatory = true)
    private SomeEnum mandatoryEnum;

    /**
     * Arguments are evaluated once as templates as startup and are fixed after that.
     * Can be used to provide static configuration via <code>args</code> files.
     * Examples:
     * <pre>
     *   argumentInteger: 1000
     *   argumentInteger: "{my-int}"
     * </pre>
     */
    @FlowKey(mandatory = true) @Argument
    private Integer argumentInteger;

    /**
     * Templates are runtime constructs.
     * As of 0.17.0, the following template grammar is supported:
     * <ul>
     *     <li><code>{T}</code>: Flow Map Key lookup</li>
     *     <li><code>{M@T}</code>: Map lookup, where <code>M</code> evaluates to a Map and <code>T</code> to a String</li>
     *     <li><code>{L}[T]</code>: List lookup, where <code>L</code> evaluates to a List and <code>T</code> to an Integer</li>
     *     <li><code>{T1}...{Tn}</code>: Mixed String template concatenation, every T must evaluate to a String</li>
     *     <li>Otherwise: Primitive type</li>
     * </ul>
     * The result of the evaluation needs to conform to the type given by <code>T&lt;&gt;</code>.
     * <p>
     * If the current flow map contents are
     * <pre>
     *   {
     *     my-string: "string"
     *     my-map: {"key": "anotherstring"}
     *   }
     * </pre>
     * then valid definitions for this key are
     * <pre>
     *  simpleTemplate: "justastring"
     *  simpleTemplate: "{my-string}"
     *  simpleTemplate: "{{my-map}@key}"
     * </pre>
     * Invalid definitions for this key:
     * <pre>
     *  simpleTemplate: 42      # not a string!
     *  simpleTemplate: "{x}"   # x does not exist in the current flow map
     *  simpleTemplate: "{{my-map}@my-mp}"   # my-map does not contain 'my-mp'
     *  simpleTemplate: "{{my-map}}[0]"      # my-map is not a list
     * </pre>
     */
    @FlowKey(mandatory = true)
    private final T<String> simpleTemplate = new T<>(){};

    /**
     * Target types which can be mapped to the Java type system are possible.
     * Type mismatch will cause Runtime exceptions.
     */
    @FlowKey(mandatory = true)
    private final T<Map<String, Integer>> mapTemplate = new T<>(){};

    /**
     * Optional keys do not need to be defined.
     */
    @FlowKey
    private String optionalNull;
    /**
     * Some keys can have default (JSON) values.
     */
    @FlowKey(defaultValue = "\"base42\"")
    private String optionalBase;
    /**
     * Templates behave the same as non templates for the optional case.
     */
    @FlowKey
    private T<String> optionalNullTemplate = new T<>(){};
    /**
     * Templates behave the same as non templates for the default value case.
     */
    @FlowKey(defaultValue = "\"base42\"")
    private T<String> optionalBaseTemplate = new T<>(){};
    /**
     * Additionally, the default value could be a template.
     */
    @FlowKey(defaultValue = "\"{url}\"")
    private T<String> optionalDefaultTemplate = new T<>(){};
    /**
     * Default values can be any valid JSON object.
     */
    @FlowKey(defaultValue = "{\"key\": \"{url}\"}")
    private T<Map<String, String>> optionalDefaultTemplateComplex = new T<>(){};

    /**
     * Mixing templates and actual types is allowed.
     */
    @FlowKey(defaultValue = "{\"actual\": 1, \"replaced\": \"{id}\"}")
    private T<Map<String, Integer>> multiTemplate = new T<>(){};

    @Override @NotNull
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) {
    }


    @SuppressWarnings("unused") // used for enum parsing
    enum SomeEnum {
        VALUE_A, VALUE_B
    }
}
