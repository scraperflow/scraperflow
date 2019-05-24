package scraper.nodes.apptest;

import scraper.annotations.node.NodePlugin;
import scraper.annotations.node.FlowKey;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.core.NodeLogLevel;
import scraper.core.Template;
import scraper.api.exceptions.NodeException;

import java.util.List;

import static scraper.core.NodeLogLevel.ERROR;


/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1")
public final class NoNode extends AbstractNode {

    @FlowKey(defaultValue = "false")
    private Boolean fail;

    @Override
    public void accept(final FlowMap o) throws NodeException {
        System.setProperty("done", "true");
        if(fail) {
            log(ERROR,"EXP");
            throw new NodeException("abc");
        }
    }
}
