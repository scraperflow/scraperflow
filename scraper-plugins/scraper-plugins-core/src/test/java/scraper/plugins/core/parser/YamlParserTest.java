package scraper.plugins.core.parser;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.exceptions.ValidationException;

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
            Assert.assertTrue(p.parseSingle(validFile).isPresent());
        }
    }

    @Test
    public void parseInvalidYmlSpecifications() {
        URL folder = (getClass().getResource("yaml/invalid"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        Assert.assertNotNull(validFiles);
        for (File validFile : validFiles) {
            Assert.assertFalse(p.parseSingle(validFile).isPresent());
        }
    }
}