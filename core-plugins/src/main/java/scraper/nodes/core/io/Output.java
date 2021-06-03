package scraper.nodes.core.io;

import scraper.annotations.FlowKey;
import scraper.annotations.Io;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.T;

/**
 * Outputs a string line to the environment
 */
@NodePlugin("0.2.0")
@Io
public final class Output implements FunctionalNode {

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
