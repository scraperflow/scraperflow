package scraper.nodes.core.example;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;
import scraper.core.template.TemplateExpression;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.io.File;
import java.util.Map;

import static scraper.api.node.container.NodeLogLevel.INFO;

// AbstractNode provides all expected features
@NodePlugin(deprecated = true)
public class HelloWorldNode implements Node {

    @FlowKey(mandatory = true) // the field 'hello' is expected to be defined in the scrape file; otherwise exception before start
    @Argument // signifies that this field should be replaced with arguments of the forwarded map once on initialization
              // i.e. they are 'final' after initialization and should not be modified again
              // @Argument and Ts do not work together
    private String hello;

    @FlowKey(mandatory = true)    // every field expected to be read from a .scrape file has to be denoted either Mandatory or Optional
    @EnsureFile(ensureDir = true) // Signifies that the String value of this field denotes a (local) path
    private String sourceFile;    // Ensures that the file exists at the path (if not, it touches the file)
                                  // ensureDir ensures that the parent directory of the file exists
                                  // if not, tries to create the directory structure

    // Optional fields can be missing in the .scrape file. Their value will be null if missing
    // The value inside the annotation is used if there is no definition in the JSON file
    @FlowKey(defaultValue = "\"Mustermann\"")
    private String name;

    // for values evaluated at runtime, one can use Ts
    // they can be evaluated by multiple threads at once without affecting one another
    // a T follows the T grammar
    // e.g. this would be a valid JSON definition
    // { "user" : 1, "user2" : "{user-id}" }
    // where user-id is replaced with the value of the key 'user-id' in the map and converted to an Integer
    @FlowKey(defaultValue = "{}")
    private T<Map<String, Integer>> typesafeMap = new T<>(){};

    @Override
    public void init(@NotNull NodeContainer<? extends Node> n, @NotNull final ScrapeInstance job) throws ValidationException {
        System.out.println("Initializing node...");
        // do some init work
    }

    @NotNull
    @Override
    public FlowMap process(NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        // processes Ts and reserves keys in the map
        // ensures that the file and directory structure denoted by 'sourceFile' exists
        // takes the evaluated T of field 'hello' and reserves the corresponding key in the map 'o'
        // start(o); // this method is executed before the process call in the accept(FlowMap) abstract function

        n.log(INFO,"Hello {}!", hello);

        // to get the raw T, one can fetch the raw JSON definition at any time
        n.log(INFO,"The T for field hello is: {}", n.getKeySpec("hello"));

        // you can use Ts on the fly like this
        String myT = "{hello}";
        TemplateExpression<String> replacedT = TemplateUtil.parseTemplate(myT);
        n.log(INFO, "{} => {}", myT, replacedT.eval(o));

        // Ts support a powerful grammar
        myT = "a prefix-{hello}-a suffix}";
        replacedT = TemplateUtil.parseTemplate(myT);
        n.log(INFO,"{} => {}", myT, replacedT);

        // fields annotated with @Optional are null if not defined in the .scrape file
        n.log(INFO,"Optional parameter: {}", (name == null ? "not provided" : name));

        n.log(INFO,"File '{}' along with its subdirectories was ensured to exist: {}",
                new File(sourceFile), new File(sourceFile).exists());

        // execute this node and follow along with the l
        n.log(INFO,"The forward method will do the following:");
        n.log(INFO,"Forwarding enabled: {}", n.isForward());
        if (n.isForward()) {
            n.log(INFO,"Goto: {}", (!n.getGoTo().isPresent() ? ("next node") : "node '" + n.getGoTo() + "'"));
        }

        // a node can eval to create sequential flows and modify control flow
        FlowMap newMap = n.eval(o, NodeUtil.addressOf("this-label-has-to-exist"));

        // forwards to another node depending on the keys 'goTo', 'forward' of the process node
        // definition in the .scrape file
        // returns after action is completed with a modified FlowMap
        return n.forward(newMap);
    }
}
