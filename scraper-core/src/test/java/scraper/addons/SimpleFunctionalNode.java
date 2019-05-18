package scraper.addons;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleFunctionalNode extends AbstractFunctionalNode {

    @FlowKey
    private String output;

    @Override
    public void modify(final FlowMap o) {
        if(output != null)
            o.put(output, true);
        else {
            o.put("simple", true);
        }
    }
}