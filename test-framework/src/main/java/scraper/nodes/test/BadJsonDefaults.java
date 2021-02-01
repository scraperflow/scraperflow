package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class BadJsonDefaults implements FunctionalNode {

    // this will raise an exception
    @FlowKey(defaultValue = "youmightthinkthisisastringbutitisnot")
    private String notastring;

    @FlowKey(defaultValue = "\"thisisastring\"")
    private String astring;

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull FlowMap o) {}
}