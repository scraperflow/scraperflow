package scraper.util;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.exceptions.ValidationException;
import scraper.api.specification.ScrapeSpecification;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class JobUtilTest {

    @Test(expected = ValidationException.class)
    public void parseMultipleYmlsTest() throws IOException, ValidationException {
        JobUtil.parseJobs(new String[]{"one.yml", "two.yml"}, Set.of());
    }

    @Test
    public void emptyParseTest() throws IOException, ValidationException {
        List<ScrapeSpecification> result = JobUtil.parseJobs(new String[]{""}, Set.of());
        Assert.assertEquals(0, result.size());
    }

    @Test // Tested by YmlUtil tests
    public void parseBasicYml() throws IOException, ValidationException {
        URL ymlToParse = (getClass().getResource("yml/basic.yml"));
        List<ScrapeSpecification> result = JobUtil.parseJobs(new String[]{ymlToParse.getPath()}, Set.of());
        Assert.assertEquals(2, result.size());
    }

    @Test(expected = ValidationException.class)
    public void parseMultipleScrapeTest() throws IOException, ValidationException {
        JobUtil.parseJobs(new String[]{"one.scrape", "two.scrape"}, Set.of());
    }
    
    @Test(expected = ValidationException.class)
    public void parseMultipleSTestTest() throws IOException, ValidationException {
        JobUtil.parseTestJob(new String[]{"one.stest", "two.stest"});
    }

    @Test
    public void impliedScrapeFileTest() throws IOException, ValidationException {
        String oldDir = System.getProperty("user.dir");
        URL t = getClass().getResource("yml");
        System.setProperty("user.dir", t.getPath());
        List<ScrapeSpecification> result = JobUtil.parseJobs(new String[0], Set.of());
        Assert.assertEquals(1, result.size());
        System.setProperty("user.dir", oldDir);
    }

    @Test
    public void directScraperFileTest() throws IOException, ValidationException {
        URL t = getClass().getResource("job/job1.scrape");
        List<ScrapeSpecification> result = JobUtil.parseJobs(new String[]{t.getPath()}, Set.of());
        Assert.assertEquals(1, result.size());
    }

    @Test(expected = ValidationException.class)
    public void multipleNdepTest() throws IOException, ValidationException {
        URL t = getClass().getResource("job/job1.scrape");
        JobUtil.parseJobs(new String[]{t.getPath(), "ndep1.ndep", "ndep2.ndep"}, Set.of());
    }

    @Test
    public void explicitNdepTest() throws IOException, ValidationException {
//        URL t = getClass().getResource("job/job1.scrape");
//        URL t2 = getClass().getResource("job/ver/job1.ndep");
//        List<ScrapeSpecification> result = JobUtil.parseJobs(new String[]{t.getPath(), t2.getPath()}, Set.of());
//        Assert.assertEquals(1, result.size());
//        Assert.assertNotNull(result.get(0).getNodeDependencyFile());
    }

    @Test
    public void envNdepArgsTest() throws IOException, ValidationException {
//        URL t = getClass().getResource("job/job1.scrape");
//
//        String oldDir = System.getProperty("user.dir");
//        URL t2 = getClass().getResource("job/ver");
//        System.setProperty("user.dir", t2.getPath());
//        List<ScrapeSpecification> result = JobUtil.parseJobs(new String[]{t.getPath()}, Set.of());
//        Assert.assertEquals(1, result.size());
//        Assert.assertNotNull(result.get(0).getNodeDependencyFile());
//        System.setProperty("user.dir", oldDir);
    }

}