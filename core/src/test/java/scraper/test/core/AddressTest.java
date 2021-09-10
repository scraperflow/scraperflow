package scraper.test.core;

import org.junit.jupiter.api.Test;
import scraper.api.node.impl.GraphAddressImpl;
import scraper.api.node.impl.InstanceAddressImpl;
import scraper.api.node.impl.NodeAddressImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static scraper.util.NodeUtil.addressOf;


public class AddressTest {


    @Test
    public void instanceAddressEqualityTest() {
        assertEquals( new InstanceAddressImpl("debug"), addressOf("debug") );
        assertEquals( addressOf("debug"), new InstanceAddressImpl("debug") );

        assertEquals( new InstanceAddressImpl("debug").hashCode(), addressOf("debug").hashCode() );
    }

    @Test
    public void graphAddressEqualityTest() {
        assertEquals( new GraphAddressImpl("debug", "graph"), addressOf("debug.graph") );
        assertEquals( addressOf("debug.graph"), new GraphAddressImpl("debug", "graph") );

        assertEquals( addressOf("debug.graph").hashCode(), new GraphAddressImpl("debug", "graph").hashCode() );
    }

    @Test
    public void nodeAddressSymmetryIndexEqualityTest() {
        assertEquals(
                addressOf("debug.graph.0"),
                new NodeAddressImpl("debug", "graph", "node", 0)
        );
        assertEquals(
                new NodeAddressImpl("debug", "graph", "node", 0),
                addressOf("debug.graph.0")
        );
    }

    @Test
    public void nodeAddressSymmetryLabelEqualityTest() {
        assertEquals(
                addressOf("debug.graph.node"),
                new NodeAddressImpl("debug", "graph", "node", 0)
        );
        assertEquals(
                new NodeAddressImpl("debug", "graph", "node", 0),
                addressOf("debug.graph.node")
        );

        assertEquals(
                new NodeAddressImpl("debug", "graph", "node", 0).hashCode(),
                addressOf("debug.graph.node").hashCode()
        );
    }

    // reflexivity
    @Test
    public void addressReflexivityTest() {
        assertEquals(
                addressOf("debug.graph.2"),
                addressOf("debug.graph.2")
        );
        assertEquals(
                addressOf("debug.graph.node"),
                addressOf("debug.graph.node")
        );

        assertEquals(
                addressOf("debug.graph.2").hashCode(),
                addressOf("debug.graph.2").hashCode()
        );
    }

    @Test
    public void graphAddressReflexivityTest() {
        assertEquals(
                new GraphAddressImpl("debug", "graph"),
                new GraphAddressImpl("debug", "graph")
        );

        assertEquals(
                new GraphAddressImpl("debug", "graph").hashCode(),
                new GraphAddressImpl("debug", "graph").hashCode()
        );
    }

    @Test
    public void instanceAddressReflexivityTest() {
        assertEquals(
                new InstanceAddressImpl("debug"),
                new InstanceAddressImpl("debug")
        );

        assertEquals(
                new InstanceAddressImpl("debug").hashCode(),
                new InstanceAddressImpl("debug").hashCode()
        );
    }

    // label and index together is only allowed for NodeAddress
    @Test
    public void forbidInferringLabelAndIndexRelationTest() {
        assertThrows(Exception.class, () -> addressOf("debug.graph.node:0") );
    }

}