package scraper.core;

import org.junit.Test;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.ValidationException;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.util.DependencyInjectionUtil;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class JobFactoryTest {

//    private final DIContainer deps = DependencyInjectionUtil.getDIContainer();
//
//    @Test
//    public void createEmptyJobTest() {
//        JobFactory factory = deps.get(JobFactory.class);
//        ScrapeInstaceImpl empty = factory.createEmptyJob();
//        assertEquals(0, empty.process.size());
//    }
//
//    @Test
//    public void convertScrapeJobTest() throws IOException, ValidationException {
//        URL base = getClass().getResource("jobfactory");
//        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
//        specification.basePath(base.getPath());
//        specification.scrapeFile("job1.yf");
//        specification.name("job1");
//        specification.nodeDependencyFile("job1.ndep");
//        specification.argumentFile("level0.args");
//        specification.argumentFile("level1.args");
//
//        JobFactory factory = deps.get(JobFactory.class);
//        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
//
//        assertEquals(2, instance.initialArguments.size());
//        assertEquals("ok", instance.initialArguments.get("arg1"));
//        assertEquals("42", instance.initialArguments.get("l1"));
//
//        assertEquals(2, instance.process.size());
//    }
//
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
//
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