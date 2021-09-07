package scraper.plugins.core.flowgraph.core.time;

import org.junit.jupiter.api.Test;
import scraper.api.ScrapeInstance;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static scraper.plugins.core.flowgraph.ResourceUtil.read;

public class PeriodicNodeTest {
    @Test
    public void regexAsStreamTest() throws Exception {
        ScrapeInstance spec = read("core/time/periodic1.jf");
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);

        assertEquals(5, cfg.getNodes().size());
        assertEquals(3, cfg.getEdges().size());
    }
}
