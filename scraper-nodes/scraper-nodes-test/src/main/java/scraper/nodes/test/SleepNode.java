package scraper.nodes.test;


import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.api.exceptions.NodeException;

@NodePlugin(deprecated = true)
public class SleepNode extends AbstractNode {
    private @FlowKey(mandatory = true) Integer sleep;

    @Override
    public FlowMap process(FlowMap o) throws NodeException {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return forward(o);
    }

}
