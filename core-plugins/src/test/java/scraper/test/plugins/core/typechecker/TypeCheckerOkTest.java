package scraper.test.plugins.core.typechecker;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import scraper.api.DIContainer;
import scraper.api.ScrapeInstance;
import scraper.api.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.typechecker.TypeChecker;
import scraper.test.plugins.core.flowgraph.helper.InstanceHelper;
import scraper.util.DependencyInjectionUtil;
import scraper.utils.ClassUtil;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Stream;

public class TypeCheckerOkTest {

    public static Stream<File> pathProvider() throws Exception {
        URL resource = ClassUtil.getResourceUrl(TypeCheckerOkTest.class, "ok");

        File files = new File(resource.toURI());
        File[] allFiles = files.listFiles();
        assert allFiles != null;
        return Arrays.stream(allFiles);
    }

    @ParameterizedTest
    @MethodSource("pathProvider")
    public void testOkCheck(File path) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%3$s | %5$s %n");

        TypeChecker t = new TypeChecker();
        ScrapeInstance spec = read(path);
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(spec);
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
