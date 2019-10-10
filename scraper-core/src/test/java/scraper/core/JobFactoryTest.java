package scraper.core;

import org.junit.Test;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.ValidationException;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.util.DependencyInjectionUtil;
import scraper.util.JobUtil;

import java.io.IOException;
import java.net.URL;
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

        assertEquals(2, instance.initialArguments.size());
        assertEquals("ok", instance.initialArguments.get("arg1"));
        assertEquals("42", instance.initialArguments.get("l1"));

        assertEquals(2, instance.getEntryGraph().size());
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