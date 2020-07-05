package scraper.plugins.core.parser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class JsonParserTest {

    static JsonParser p = new JsonParser();

    @Test
    public void parseValidJsonSpecifications() {
        URL folder = (getClass().getResource("json/valid"));

        File[] validFiles = new File(folder.getPath()).listFiles();
        assertNotNull(validFiles);

        Arrays.stream(validFiles).forEach(f -> assertTrue(p.parseSingle(f).isPresent()));
    }

    @Test
    public void parseInvalidJsonSpecifications() {
        URL folder = (getClass().getResource("json/invalid"));
        File[] validFiles = new File(folder.getPath()).listFiles();
        assertNotNull(validFiles);
        for (File validFile : validFiles) {
            assertFalse( p.parseSingle(validFile).isPresent() );
        }
    }
}