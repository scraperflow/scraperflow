package scraper.nodes.core.test.addons;


import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractNode;
import scraper.api.exceptions.NodeException;

@NodePlugin
public class SleepNode extends AbstractNode {
    private @FlowKey(mandatory = true) Integer sleep;

    @NotNull
    @Override
    public FlowMap process(@NotNull FlowMap o) throws NodeException {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return forward(o);
    }

}
