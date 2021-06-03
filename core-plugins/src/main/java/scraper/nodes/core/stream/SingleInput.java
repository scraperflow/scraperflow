package scraper.nodes.core.stream;

import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.L;

import java.util.Scanner;

/**
 * Processes a string input line from the environment
 */
@NodePlugin("0.2.0")
public final class SingleInput implements FunctionalNode {

    /** String output */
    @FlowKey(mandatory = true)
    private final L<String> put = new L<>(){};

    @Override
    public void modify(FunctionalNodeContainer n, FlowMap o) {
        Scanner s = new Scanner(System.in);
        if(s.hasNext()) o.output(put, s.next());
    }
}
