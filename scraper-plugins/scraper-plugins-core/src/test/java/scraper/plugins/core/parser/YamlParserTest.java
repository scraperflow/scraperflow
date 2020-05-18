package scraper.plugins.core.parser;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.exceptions.ValidationException;
import scraper.api.specification.ScrapeSpecification;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class YamlParserTest {

    static YamlParser p = new YamlParser();

    @Test
    public void parseValidYmlSpecifications() throws IOException, ValidationException {
        URL folder = (getClass().getResource("yaml/valid"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        Assert.assertNotNull(validFiles);
        for (File validFile : validFiles) {
            ScrapeSpecification result = p.parseSingle(validFile);
        }
    }

    @Test
    public void parseInvalidYmlSpecifications() {
        URL folder = (getClass().getResource("yaml/invalid"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        Assert.assertNotNull(validFiles);
        for (File validFile : validFiles) {
            try {
                p.parseSingle(validFile);
                Assert.fail("Expected exception for file " + validFile.getPath());
            } catch (Exception ignored) {}
        }
    }
}