package scraper.plugins.core.flowgraph.core.flow;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.impl.AddressImpl;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.GraphVisualizer;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static scraper.plugins.core.flowgraph.ResourceUtil.*;

public class PipeNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/flow/pipe1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);


        Assert.assertEquals(5, cfg.getNodes().size());
        Assert.assertEquals(4, cfg.getEdges().size());

        Address firstNode = opt(() -> spec.getNode("<pipe1.start.1>")).getAddress();
        Assert.assertEquals(1, cfg.getIncomingEdges(firstNode).size());
        Assert.assertEquals(3, cfg.getOutgoingEdges(firstNode).size());

    }


    @Test
    public void simplePrePostTest() throws Exception {
        ScrapeInstance spec = read("core/flow/pipe1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        Address pipe = of("pipe1.start.1");


        Assert.assertEquals(1, cfg.pre(pipe).size());
        Assert.assertEquals(3, cfg.post(pipe).size());
    }
}
