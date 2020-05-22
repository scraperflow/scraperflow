package scraper.plugins.core.flowgraph.core.flow;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.node.Address;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.GraphVisualizer;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static scraper.plugins.core.flowgraph.ResourceUtil.opt;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class MapJoinNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/flow/mapjoin1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        Assert.assertEquals(4, cfg.getNodes().size());
        Assert.assertEquals(3, cfg.getEdges().size());

        Address firstNode = opt(() -> spec.getNode("<mapjoin1.start.1>")).getAddress();
        Assert.assertEquals(1, cfg.getIncomingEdges(firstNode).size());
        Assert.assertEquals(2, cfg.getOutgoingEdges(firstNode).size());
    }
}
