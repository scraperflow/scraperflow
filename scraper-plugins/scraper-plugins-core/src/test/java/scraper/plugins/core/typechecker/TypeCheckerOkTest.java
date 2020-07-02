package scraper.plugins.core.typechecker;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RunWith(Parameterized.class)
public class TypeCheckerOkTest {

    private File path;

    public TypeCheckerOkTest(File f) {
        this.path = f;
    }

    @Parameterized.Parameters
    public static Collection primeNumbers() throws Exception {
        URL resource = TypeCheckerOkTest.class.getResource("ok");
        File files = new File(resource.toURI());
        File[] allFiles = files.listFiles();
        assert allFiles != null;
        return Arrays.stream(allFiles).map(f -> new Object[]{ f }).collect(Collectors.toList());
    }

    @Test
    public void testOkCheck() {
        try {
            TypeChecker t = new TypeChecker();
            ScrapeInstance spec = read(path);
            ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec, true);
            t.typeTaskflow(spec, cfg);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private static ScrapeInstance read(File f) {
        try {
            ScrapeSpecification spec = InstanceHelper.getInstance(f);
            DIContainer dibean = DependencyInjectionUtil.getDIContainer();

            return dibean.get(JobFactory.class).convertScrapeJob(spec);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
