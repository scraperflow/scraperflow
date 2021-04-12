package scraper.nodes.apptest;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.util.List;


/**
 * @author Albert Schimpf
 */
@NodePlugin(value = "0.0.1", deprecated = true)
public final class RuntimeList implements Node {

    @FlowKey(mandatory = true)
    private T<List<String>> onlyStringsAllowed = new T<>(){};

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) {
        List<String> myListWithStrings = o.eval(onlyStringsAllowed);
        System.out.println(myListWithStrings);
        return o;
    }
}
