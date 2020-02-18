package scraper.plugins.core.flowgraph.core.time;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.node.Address;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static scraper.plugins.core.flowgraph.ResourceUtil.opt;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class PeriodicNodeTest {
    @Test
    public void regexAsStreamTest() throws Exception {
        ScrapeInstance spec = read("core/time/periodic1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        Assert.assertEquals(5, cfg.getNodes().size());
        Assert.assertEquals(3, cfg.getEdges().size());
    }
}
