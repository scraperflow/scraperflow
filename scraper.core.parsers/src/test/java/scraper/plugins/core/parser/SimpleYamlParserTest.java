package scraper.plugins.core.parser;

import org.junit.jupiter.api.Test;
import scraper.api.ScrapeSpecificationParser;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleYamlParserTest {

    static ScrapeSpecificationParser p = new SimpleYamlParser();

    @Test
    public void parseValidYmlSpecifications() {
        URL folder = (getClass().getResource("simpleyaml/valid"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        assertNotNull(validFiles);
        for (File validFile : validFiles) {
            assertTrue(p.parseSingle(validFile).isPresent());
        }
    }

    @Test
    public void parseInvalidYmlSpecifications() {
        // full yml specifications are not valid simple yaml specifications
        URL folder = (getClass().getResource("yaml/valid"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        assertNotNull(validFiles);
        for (File validFile : validFiles) {
            assertFalse(p.parseSingle(validFile).isPresent());
        }
    }
}