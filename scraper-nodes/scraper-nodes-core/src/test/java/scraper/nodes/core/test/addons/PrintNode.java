package scraper.nodes.core.test.addons;

import scraper.annotations.node.NodePlugin;
import scraper.annotations.node.FlowKey;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.core.NodeLogLevel;
import scraper.core.Template;
import scraper.api.exceptions.NodeException;

import java.util.List;


/**
 * Logs specific arguments in the current flow map. Useful for debugging.
 */
@NodePlugin(value = "0.1.1")
public final class PrintNode extends AbstractNode {

    @FlowKey
    private Template<String> arg = new Template<>(){};

    @FlowKey
    private Template<List<String>> args = new Template<>(){};

    @FlowKey
    private String key;

    @Override
    public FlowMap process(final FlowMap o) throws NodeException {
        String arg = this.arg.eval(o);
        List<String> args = this.args.eval(o);

        if(arg != null)
            log(NodeLogLevel.INFO, arg);

        if(args != null)
            args.forEach(e->log(NodeLogLevel.INFO, e));

        if(key != null)
            log(NodeLogLevel.INFO, String.valueOf(o.get(key)));

        return forward(o);
    }
}
