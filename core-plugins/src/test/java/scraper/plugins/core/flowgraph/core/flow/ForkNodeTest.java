package scraper.plugins.core.flowgraph.core.flow;

import org.junit.jupiter.api.Test;
import scraper.api.Address;
import scraper.api.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static scraper.plugins.core.flowgraph.ResourceUtil.opt;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class ForkNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/flow/fork1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        assertEquals(5, cfg.getNodes().size());
        assertEquals(4, cfg.getEdges().size());

        Address firstNode = opt(() -> spec.getNode("<fork1.start.1>")).getAddress();
        assertEquals(1, cfg.getIncomingEdges(firstNode).size());
        assertEquals(3, cfg.getOutgoingEdges(firstNode).size());
    }
}
