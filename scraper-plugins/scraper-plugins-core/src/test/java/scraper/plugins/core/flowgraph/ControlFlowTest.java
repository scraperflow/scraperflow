package scraper.plugins.core.flowgraph;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.node.NodeAddress;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class ControlFlowTest {

    @Test
    public void simpleTest() throws Exception {
        ScrapeInstance spec = read("simpletest/simple.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        Assert.assertEquals(1, cfg.getNodes().size());
        Assert.assertTrue(cfg.getEdges().isEmpty());
    }

    @Test
    public void simple2Test() throws Exception {
        ScrapeInstance spec = read("simpletest/simple2.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        Assert.assertEquals(3, cfg.getNodes().size());
        Assert.assertEquals(2, cfg.getEdges().size());

        NodeAddress firstNode = spec.getEntryGraph().get(0).getAddress();
        Assert.assertTrue(cfg.getIncomingEdges(firstNode).isEmpty());
        Assert.assertEquals(1, cfg.getOutgoingEdges(firstNode).size());

        NodeAddress secondNode = spec.getEntryGraph().get(1).getAddress();
        Assert.assertEquals(1, cfg.getIncomingEdges(secondNode).size());
        Assert.assertEquals(cfg.getOutgoingEdges(firstNode).size(), cfg.getIncomingEdges(secondNode).size());
        Assert.assertEquals(cfg.getOutgoingEdges(firstNode).get(0), cfg.getIncomingEdges(secondNode).get(0));
    }


    @Test
    public void twoGraphsTest() throws Exception {
        ScrapeInstance spec = read("simpletest/twographs.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        Assert.assertEquals(3, cfg.getNodes().size());
        Assert.assertEquals(2, cfg.getEdges().size());

        NodeAddress firstNode = spec.getEntryGraph().get(0).getAddress();
        Assert.assertEquals(1, cfg.getIncomingEdges(firstNode).size());
        Assert.assertEquals(1, cfg.getOutgoingEdges(firstNode).size());
        NodeAddress secondNode = spec.getEntryGraph().get(1).getAddress();
        Assert.assertEquals(0, cfg.getOutgoingEdges(secondNode).size());
    }
}

