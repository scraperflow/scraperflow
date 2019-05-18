package scraper.util;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.specification.ScrapeSpecification;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class YmlParseTest {

    @Test
    public void basicYmlFileTest() throws IOException {
        URL basePath = getClass().getResource("yml");

        URL ymlToParse = (getClass().getResource("yml/basic.yml"));
        List<ScrapeSpecification> spec = YmlParse.parseYmlFile(ymlToParse.getPath(), Collections.emptySet());
        Assert.assertEquals(2, spec.size());

        ScrapeSpecification spec1 = spec.get(0);
        ScrapeSpecification spec2 = spec.get(1);

        Assert.assertEquals("job1.scrape", spec1.getScrapeFile());
        String path = basePath.getPath();
        if (basePath.getPath().endsWith("/")) { path = path.substring(0, path.length()-1); }
        Assert.assertEquals(path, spec1.getBasePath());
        Assert.assertEquals(0, spec1.getPaths().size());
        Assert.assertEquals("somejob1", spec1.getName());
        Assert.assertEquals(1, spec1.getArgumentFiles().size());
        Assert.assertEquals("job1.args", spec1.getArgumentFiles().get(0));


        // order of args important
        Assert.assertEquals(3, spec2.getArgumentFiles().size());
        Assert.assertEquals("notimportant.args", spec2.getArgumentFiles().get(0));
        Assert.assertEquals("job1.args", spec2.getArgumentFiles().get(1));
        Assert.assertEquals("important.args", spec2.getArgumentFiles().get(2));
    }

    @Test(expected = IllegalStateException.class)
    public void YmlFileMissingScrapeTest() throws IOException {
        URL ymlToParse = (getClass().getResource("yml/bad.yml"));
        YmlParse.parseYmlFile(ymlToParse.getPath(), Collections.emptySet());
    }
}