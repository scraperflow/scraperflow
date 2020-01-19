package scraper.nodes.core.test.addons;


import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.api.exceptions.NodeException;

@NodePlugin
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
