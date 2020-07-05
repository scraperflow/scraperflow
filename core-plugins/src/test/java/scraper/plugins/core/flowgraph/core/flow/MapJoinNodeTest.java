package scraper.plugins.core.flowgraph.core.flow;

import org.junit.jupiter.api.Test;
import scraper.api.node.Address;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static scraper.plugins.core.flowgraph.ResourceUtil.opt;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class MapJoinNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/flow/mapjoin1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        assertEquals(4, cfg.getNodes().size());
        assertEquals(3, cfg.getEdges().size());

        Address firstNode = opt(() -> spec.getNode("<mapjoin1.start.1>")).getAddress();
        assertEquals(1, cfg.getIncomingEdges(firstNode).size());
        assertEquals(2, cfg.getOutgoingEdges(firstNode).size());
    }
}
