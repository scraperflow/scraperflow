package scraper.plugins.core.flowgraph.core.functional;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.node.NodeAddress;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.DataFlowGraph;

import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class EchoNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/functional/echo1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        Assert.assertEquals(1, cfg.getNodes().size());
        Assert.assertTrue(cfg.getEdges().isEmpty());
    }

    @Test
    public void emptyDfgTest() throws Exception {
        ScrapeInstance spec = read("core/functional/echo1.jf");
        DataFlowGraph dfg = FlowUtil.generateDataFlowGraph(spec);

        Assert.assertEquals(1, dfg.getNodes().size());

        NodeAddress address = spec.getEntryGraph().get(0).getAddress();
        Assert.assertTrue(dfg.getDataFlowFor(address).produces().isEmpty());

        // log and puts
        Assert.assertEquals(2, dfg.getDataFlowFor(address).consumes().size());
        Assert.assertTrue(dfg.getDataFlowFor(address).consumes().containsKey("log"));
        Assert.assertTrue(dfg.getDataFlowFor(address).consumes().containsKey("puts"));
    }

    @Test
    public void simpleDfgTest() throws Exception {
        ScrapeInstance spec = read("core/functional/echo2.jf");
        DataFlowGraph dfg = FlowUtil.generateDataFlowGraph(spec);

        Assert.assertEquals(4, dfg.getNodes().size());

        {
            NodeAddress onlyPuts = spec.getEntryGraph().get(0).getAddress();
            Assert.assertEquals(1, dfg.getDataFlowFor(onlyPuts).produces().size());
            Assert.assertTrue(dfg.getDataFlowFor(onlyPuts).produces().containsKey("hello"));
        }

        {
            NodeAddress onlyRemove = spec.getEntryGraph().get(1).getAddress();
            Assert.assertEquals(0, dfg.getDataFlowFor(onlyRemove).produces().size());
        }

        {
            NodeAddress both = spec.getEntryGraph().get(2).getAddress();
            Assert.assertEquals(2, dfg.getDataFlowFor(both).produces().size());
        }
    }
}
