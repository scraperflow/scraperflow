package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.List;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleListGoToNode implements Node {

    @FlowKey
    private T<List<Address>> goToList = new T<>(){};

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        return o;
    }
}