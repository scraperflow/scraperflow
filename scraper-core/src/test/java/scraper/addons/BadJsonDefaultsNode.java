package scraper.addons;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.core.AbstractFunctionalNode;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class BadJsonDefaultsNode extends AbstractFunctionalNode {

    // this will raise an exception
    @FlowKey(defaultValue = "youmightthinkthisisastringbutitisnot")
    private String notastring;

    @FlowKey(defaultValue = "\"thisisastring\"")
    private String astring;

    @Override
    public void modify(final FlowMap o) {}
}