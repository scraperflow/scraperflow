package scraper.core;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.di.DIContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.util.DependencyInjectionUtil;
import scraper.util.JobUtil;
import scraper.util.NodeUtil;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class JobFactoryTest {

    private final DIContainer deps = DependencyInjectionUtil.getDIContainer();

//    @Test
//    public void createEmptyJobTest() {
//        JobFactory factory = deps.get(JobFactory.class);
//        ScrapeInstaceImpl empty = factory.createEmptyJob();
//        assertEquals(0, empty.process.size());
//    }

    @Test
    public void convertScrapeJobTest() throws Exception {
        URL base = getClass().getResource("jobfactory/convert");
        ScrapeSpecificationImpl spec = (ScrapeSpecificationImpl) JobUtil.parseJobs(new String[]{"job1.jf", "level0.args", "level1.args"}, Set.of(base.getFile())).get(0);
        ScrapeInstaceImpl instance = deps.get(JobFactory.class).convertScrapeJob(spec);

        assertEquals(2, instance.getEntryArguments().size());
        assertEquals("ok", instance.getEntryArguments().get("arg1"));
        assertEquals("42", instance.getEntryArguments().get("l1"));
    }

    @Test
    public void resolvingAddressesTest() throws Exception {
        URL base = getClass().getResource("naming");
        ScrapeSpecificationImpl spec = (ScrapeSpecificationImpl) JobUtil.parseJobs(new String[]{"root.yf"}, Set.of(base.getFile())).get(0);
        ScrapeInstaceImpl instance = deps.get(JobFactory.class).convertScrapeJob(spec);

        // test routing table
        List.of("root","root.start","root.start.0","root.start.first","root.start.1","root.end","root.end.0",
                "import","import.start", "import.start.first","import.import","import.import.import")
                .forEach(a ->
                        Assert.assertTrue("Missing in routing table: "+a +"\n"+instance.getRoutes().keySet(),
                                instance.getNode(NodeUtil.addressOf(a)).isPresent())
                );

        // 1 -> 2
        Optional<NodeContainer<? extends Node>> n1 = instance.getNode(NodeUtil.addressOf("root"));
        Optional<NodeContainer<? extends Node>> n2 = instance.getNode(NodeUtil.addressOf("root.start.1"));
        Assert.assertTrue(n1.isPresent() && n2.isPresent() && n1.get().getGoTo().isPresent());
        Assert.assertEquals(n1.get().getGoTo().get(), n2.get());

        // 2 -> 3
        Optional<NodeContainer<? extends Node>> n3 = instance.getNode(NodeUtil.addressOf("root.end"));
        Assert.assertTrue(n3.isPresent() && n2.get().getGoTo().isPresent());
        Assert.assertEquals(n2.get().getGoTo().get(), n3.get());

        // 3 -> 4
        Optional<NodeContainer<? extends Node>> n4 = instance.getNode(NodeUtil.addressOf("import"));
        Assert.assertTrue(n4.isPresent() && n3.get().getGoTo().isPresent());
        Assert.assertEquals(n3.get().getGoTo().get(), n4.get());

        // 4 -> 5
        Optional<NodeContainer<? extends Node>> n5 = instance.getNode(NodeUtil.addressOf("import.import.import"));
        Assert.assertTrue(n5.isPresent() && n4.get().getGoTo().isPresent());
        Assert.assertEquals(n4.get().getGoTo().get(), n5.get());
    }

    @Test
    public void importsTest() throws Exception {
        URL base = getClass().getResource("jobfactory/import");
        ScrapeSpecificationImpl spec = (ScrapeSpecificationImpl) JobUtil.parseJobs(new String[]{"import-simple.jf"}, Set.of(base.getFile())).get(0);
        Assert.assertEquals(1, spec.getImports().size());
        Assert.assertNotNull(spec.getImports().get("part.jf").getSpec());
    }

//    @Test(expected = ValidationException.class)
//    public void missingPluginTest() throws IOException, ValidationException {
//        URL base = getClass().getResource("missingplugin");
//        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
//        specification.basePath(base.getPath());
//        specification.scrapeFile("job1.yf");
//        specification.name("job1");
//
//        JobFactory factory = deps.get(JobFactory.class);
//        factory.convertScrapeJob(specification.build());
//    }

//    @Test
//    public void multipleVersionsTest() throws IOException, ValidationException {
//        URL base = getClass().getResource("missingplugin");
//        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
//        specification.basePath(base.getPath());
//        specification.scrapeFile("job1.yf");
//        specification.name("job1");
//
//        JobFactory factory = deps.get(JobFactory.class);
//        factory.convertScrapeJob(specification.build());
//    }
}