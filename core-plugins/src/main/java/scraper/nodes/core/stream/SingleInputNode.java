package scraper.nodes.core.stream;

import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.template.L;

import java.util.Scanner;

/**
 * Processes a string input line from the environment
 */
@NodePlugin("0.1.0")
public final class SingleInputNode implements FunctionalNode {

    /** String output */
    @FlowKey(defaultValue = "\"_\"")
    private final L<String> put = new L<>(){};


    @Override
    public void modify(FunctionalNodeContainer n, FlowMap o) {
        Scanner s = new Scanner(System.in);
        if(s.hasNext()) o.output(put, s.next());
    }
}
