package scraper.nodes.core.io;

import scraper.annotations.*;
import scraper.api.*;

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
