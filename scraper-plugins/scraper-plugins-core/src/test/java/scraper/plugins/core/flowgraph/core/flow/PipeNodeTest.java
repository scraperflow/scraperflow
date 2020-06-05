package scraper.plugins.core.flowgraph.core.flow;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.node.Address;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.util.NodeUtil;

import static scraper.plugins.core.flowgraph.ResourceUtil.opt;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class PipeNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/flow/pipe1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);


        Assert.assertEquals(6, cfg.getNodes().size());
        Assert.assertEquals(8, cfg.getEdges().size());

        Address firstNode = opt(() -> spec.getNode("<pipe1.start.1>")).getAddress();
        Assert.assertEquals(1, cfg.getIncomingEdges(firstNode).size());
        Assert.assertEquals(3, cfg.getOutgoingEdges(firstNode).size());

    }


    @Test
    public void simplePrePostTest() throws Exception {
        ScrapeInstance spec = read("core/flow/pipe1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);
        Address pipe = NodeUtil.addressOf("pipe1","start",null, 1);
        Assert.assertEquals(1, cfg.pre(pipe).size());
    }
}
