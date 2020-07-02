package scraper.plugins.core.typechecker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.TemplateException;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.helper.InstanceHelper;
import scraper.util.DependencyInjectionUtil;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;


@RunWith(Parameterized.class)
public class TypeCheckerBadTest {

    private File path;

    public TypeCheckerBadTest(File f) {
        this.path = f;
    }

    @Parameterized.Parameters
    public static Collection primeNumbers() throws Exception {
        URL resource = TypeCheckerBadTest.class.getResource("fail");
        File files = new File(resource.toURI());
        File[] allFiles = files.listFiles();
        assert allFiles != null;
        return Arrays.stream(allFiles).map(f -> new Object[]{ f }).collect(Collectors.toList());
    }

    @Test(expected = TemplateException.class)
    public void testOkCheck() {
        TypeChecker t = new TypeChecker();
        ScrapeInstance spec = read(path);
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec, true);
        t.typeTaskflow(spec, cfg);
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
