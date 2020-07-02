package scraper.plugins.core.typechecker;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.TemplateException;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.GraphVisualizer;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.helper.InstanceHelper;
import scraper.util.DependencyInjectionUtil;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class TypeCheckerFlowTest {

    @Test
    public void singleTest() throws Exception {
        URL resource = TypeCheckerFlowTest.class.getResource("ok/mapjoin5.yf");
        File file = new File(resource.toURI());
        TypeChecker t = new TypeChecker();
        ScrapeInstance spec = read(file);
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec, true);
        t.typeTaskflow(spec, cfg);
    }

    private ScrapeInstance read(File f) {
        try {
            ScrapeSpecification spec = InstanceHelper.getInstance(f);
            DIContainer dibean = DependencyInjectionUtil.getDIContainer();

            return dibean.get(JobFactory.class).convertScrapeJob(spec);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
