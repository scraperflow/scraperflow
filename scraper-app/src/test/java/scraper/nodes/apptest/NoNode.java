package scraper.nodes.apptest;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;

import static scraper.core.NodeLogLevel.ERROR;


/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1")
public final class NoNode extends AbstractNode {

    @FlowKey(defaultValue = "false")
    private Boolean fail;

    @Override
    public FlowMap process(final FlowMap o) throws NodeException {
        System.setProperty("done", "true");
        if(fail) {
            log(ERROR,"EXP");
            throw new NodeException("abc");
        }

        return o;
    }
}
