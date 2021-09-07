package scraper.plugins.core.flowgraph.core.flow;

import org.junit.jupiter.api.Test;
import scraper.api.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class IfThenElseNodeTest {
    @Test
    public void simpleCfgTest() throws Exception {
        ScrapeInstance spec = read("core/flow/ifthenelse1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        assertEquals(4, cfg.getNodes().size());
        assertEquals(3, cfg.getEdges().size());
    }
}
