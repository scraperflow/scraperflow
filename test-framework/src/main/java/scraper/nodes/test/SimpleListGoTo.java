package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.Address;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.T;

import java.util.List;

@NodePlugin(value = "0.1.0", deprecated = true)
public final class SimpleListGoTo implements Node {

    @FlowKey
    private T<List<Address>> goToList = new T<>(){};

    @NotNull
    @Override
    public void process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) { }
}