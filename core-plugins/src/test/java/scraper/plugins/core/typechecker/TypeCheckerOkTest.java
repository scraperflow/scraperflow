package scraper.plugins.core.typechecker;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import scraper.api.di.DIContainer;
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
import java.util.stream.Stream;


public class TypeCheckerOkTest {

    public static Stream<File> pathProvider() throws Exception {
        URL resource = TypeCheckerOkTest.class.getResource("ok");
        File files = new File(resource.toURI());
        File[] allFiles = files.listFiles();
        assert allFiles != null;
        return Arrays.stream(allFiles);
    }

    @ParameterizedTest
    @MethodSource("pathProvider")
    public void testOkCheck(File path) {
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
