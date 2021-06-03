package scraper.plugins.core.parser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class YamlParserTest {

    static YamlParser p = new YamlParser();

    @Test
    public void parseValidYmlSpecifications() {
        URL folder = (getClass().getResource("yaml/valid"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        assertNotNull(validFiles);
        for (File validFile : validFiles) {
            assertTrue(p.parseSingle(validFile).isPresent());
        }
    }

    @Test
    public void parseInvalidYmlSpecifications() {
        URL folder = (getClass().getResource("yaml/invalid"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        assertNotNull(validFiles);
        for (File validFile : validFiles) {
            assertFalse(p.parseSingle(validFile).isPresent());
        }
    }
}