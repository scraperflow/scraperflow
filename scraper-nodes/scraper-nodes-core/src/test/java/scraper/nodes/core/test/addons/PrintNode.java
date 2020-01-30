package scraper.nodes.core.test.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;

import java.util.List;


/**
 * Logs specific arguments in the current flow map. Useful for debugging.
 */
@NodePlugin(value = "0.1.1")
public final class PrintNode implements Node {

    @FlowKey
    private T<String> arg = new T<>(){};

    @FlowKey
    private T<List<String>> args = new T<>(){};

    @FlowKey
    private String key;

    @NotNull
    @Override
    public FlowMap process(NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        String arg = o.eval(this.arg);
        List<String> args = o.eval(this.args);

        if(arg != null)
            n.log(NodeLogLevel.INFO, arg);

        if(args != null)
            args.forEach(e->n.log(NodeLogLevel.INFO, e));

        if(key != null)
            n.log(NodeLogLevel.INFO, String.valueOf(o.get(key)));

        return n.forward(o);
    }
}
