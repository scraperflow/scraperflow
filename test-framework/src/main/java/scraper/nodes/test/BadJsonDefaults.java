package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;

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