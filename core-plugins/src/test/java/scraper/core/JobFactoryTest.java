package scraper.core;

import org.junit.jupiter.api.Test;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.util.NodeUtil;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JobFactoryTest {

    @Test
    public void convertScrapeJobTest() throws Exception {
        URL base = getClass().getResource("jobfactory/convert");

        ScrapeInstaceImpl instance = InstanceHelper.getInstance(base, "job1.jf", "level0.args", "level1.args");

        assertEquals(2, instance.getEntryArguments().size());
        assertEquals("ok", instance.getEntryArguments().get("arg1"));
        assertEquals("42", instance.getEntryArguments().get("l1"));
    }

    @Test
    public void resolvingAddressesTest() throws Exception {
        URL base = getClass().getResource("naming");

        ScrapeInstaceImpl instance = InstanceHelper.getInstance(base, "root.yf");

        // test routing table
        List.of("root","root.start","root.start.0","root.start.first","root.start.1","root.end","root.end.0",
                "import","import.start", "import.start.first","import.import","import.import.import")
                .forEach(a ->
                        assertTrue(
                                instance.getNode(NodeUtil.addressOf(a)).isPresent(),
                                "Missing in routing table: "+a +"\n"+instance.getRoutes().keySet()
                        )
                );

        // 1 -> 2
        Optional<NodeContainer<? extends Node>> n1 = instance.getNode(NodeUtil.addressOf("root"));
        Optional<NodeContainer<? extends Node>> n2 = instance.getNode(NodeUtil.addressOf("root.start.1"));
        assertTrue(n1.isPresent() && n2.isPresent() && n1.get().getGoTo().isPresent());
        assertEquals(n1.get().getGoTo().get(), n2.get());

        // 2 -> 3
        Optional<NodeContainer<? extends Node>> n3 = instance.getNode(NodeUtil.addressOf("root.end"));
        assertTrue(n3.isPresent() && n2.get().getGoTo().isPresent());
        assertEquals(n2.get().getGoTo().get(), n3.get());

        // 3 -> 4
        Optional<NodeContainer<? extends Node>> n4 = instance.getNode(NodeUtil.addressOf("import"));
        assertTrue(n4.isPresent() && n3.get().getGoTo().isPresent());
        assertEquals(n3.get().getGoTo().get(), n4.get());

        // 4 -> 5
        Optional<NodeContainer<? extends Node>> n5 = instance.getNode(NodeUtil.addressOf("import.import.import"));
        assertTrue(n5.isPresent() && n4.get().getGoTo().isPresent());
        assertEquals(n4.get().getGoTo().get(), n5.get());
    }
}