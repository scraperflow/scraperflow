package scraper.plugins.core.flowgraph;

import org.junit.jupiter.api.Test;
import scraper.api.Address;
import scraper.api.ScrapeInstance;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static scraper.plugins.core.flowgraph.ResourceUtil.opt;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class ControlFlowTest {

    @Test
    public void simpleTest() throws Exception {
        ScrapeInstance spec = read("simpletest/simple.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        assertEquals(1, cfg.getNodes().size());
        assertTrue(cfg.getEdges().isEmpty());
    }

    @Test
    public void simple2Test() throws Exception {
        ScrapeInstance spec = read("simpletest/simple2.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        assertEquals(3, cfg.getNodes().size());
        assertEquals(2, cfg.getEdges().size());

        Address firstNode = opt(() -> spec.getNode("<simple2>")).getAddress();
        assertTrue(cfg.getIncomingEdges(firstNode).isEmpty());
        assertEquals(1, cfg.getOutgoingEdges(firstNode).size());

        Address secondNode = opt(() -> spec.getNode("<simple2.start.1>")).getAddress();
        assertEquals(1, cfg.getIncomingEdges(secondNode).size());
        assertEquals(cfg.getOutgoingEdges(firstNode).size(), cfg.getIncomingEdges(secondNode).size());
        assertEquals(cfg.getOutgoingEdges(firstNode).get(0), cfg.getIncomingEdges(secondNode).get(0));
    }

    @Test
    public void twoGraphsTest() throws Exception {
        ScrapeInstance spec = read("simpletest/twographs.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        assertEquals(3, cfg.getNodes().size());
        assertEquals(2, cfg.getEdges().size());

        Address firstNode = opt(() -> spec.getNode("<twographs>")).getAddress();
        assertEquals(1, cfg.getIncomingEdges(firstNode).size());
        assertEquals(1, cfg.getOutgoingEdges(firstNode).size());
        Address secondNode = opt(() -> spec.getNode("<twographs.start.1>")).getAddress();
        assertEquals(0, cfg.getOutgoingEdges(secondNode).size());
    }
}

