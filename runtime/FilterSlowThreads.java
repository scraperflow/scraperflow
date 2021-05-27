package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.type.Node;
import scraper.api.template.L;
import scraper.api.template.T;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Filters slow threads
 */
@NodePlugin("0.0.1")
public class FilterSlowThreads implements Node {

    /** Speed */
    @FlowKey(mandatory = true)
    private final T<String> speed = new T<>(){};

    /** Speed limit */
    @FlowKey(mandatory = true)
    private final T<Double> limit = new T<>(){};

    @Override
    public void process(NodeContainer<? extends Node> n, FlowMap o) throws NodeException {
        var speed = o.eval(this.speed);
        var speedF = Float.parseFloat(speed);

        if(speedF < o.eval(limit)) return;

        n.forward(o);
    }
}
