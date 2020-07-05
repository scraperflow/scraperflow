package scraper.plugins.core.flowgraph.core.flow;

import org.junit.jupiter.api.Test;
import scraper.api.node.Address;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.util.NodeUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static scraper.plugins.core.flowgraph.ResourceUtil.opt;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class PipeNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/flow/pipe1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);


        assertEquals(6, cfg.getNodes().size());
        assertEquals(8, cfg.getEdges().size());

        Address firstNode = opt(() -> spec.getNode("<pipe1.start.1>")).getAddress();
        assertEquals(1, cfg.getIncomingEdges(firstNode).size());
        assertEquals(3, cfg.getOutgoingEdges(firstNode).size());

    }


    @Test
    public void simplePrePostTest() throws Exception {
        ScrapeInstance spec = read("core/flow/pipe1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);
        Address pipe = NodeUtil.addressOf("pipe1","start",null, 1);
        assertEquals(1, cfg.pre(pipe).size());
    }
}
