package scraper.plugins.core.flowgraph.core.flow;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class IfThenElseNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/flow/ifthenelse1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        Assert.assertEquals(4, cfg.getNodes().size());
        Assert.assertEquals(3, cfg.getEdges().size());
    }
}
