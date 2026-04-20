package scraper.test.core;

import org.junit.jupiter.api.Test;
import scraper.annotations.NotNull;
import scraper.api.Node;
import scraper.core.AbstractMetadata;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractMetadataTest {
    @Test
    public void simpleData() {
        AbstractMetadata datam = new AbstractMetadata("node", "1.0.1", "nodecat", true) {
            @NotNull
            @Override public Node getNode() { return null; }
        };
        AbstractMetadata data = new AbstractMetadata("node", "1.0.2", "nodecat", true) {
            @NotNull
            @Override public Node getNode() { return null; }
        };

        AbstractMetadata dataa = new AbstractMetadata("node", "1.2.2", "nodecat", true) {
            @NotNull
            @Override public Node getNode() { return null; }
        };

        AbstractMetadata data2 = new AbstractMetadata("node", "2.0.2", "nodecat", false) {
            @NotNull
            @Override public Node getNode() { return null; }
        };

        assertEquals("nodecat", data.getCategory());

        assertFalse(data.backwardsCompatible(data2));
        assertFalse(data2.backwardsCompatible(data));

        assertFalse(data.backwardsCompatible(dataa));
        assertTrue(dataa.backwardsCompatible(data));

        assertTrue(data.backwardsCompatible(datam));
        assertFalse(datam.backwardsCompatible(data));
    }

}