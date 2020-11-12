package scraper.nodes.core.io;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.T;

/**
 * Outputs a string line to the environment
 */
@NodePlugin("0.2.0")
public final class OutputNode implements FunctionalNode {

    /** String output line */
    @FlowKey(mandatory = true)
    private final T<String> output = new T<>(){};

    /** Output with newline or without */
    @FlowKey(defaultValue = "true")
    private Boolean line;

    @Override
    public void modify(FunctionalNodeContainer n, FlowMap o) {
        String output = o.eval(this.output);
        if(line) System.out.println(output);
        else System.out.print(output);
    }
}
