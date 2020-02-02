package scraper.core;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.impl.GraphAddressImpl;
import scraper.api.node.impl.InstanceAddressImpl;
import scraper.api.node.impl.NodeAddressImpl;
import scraper.api.node.type.Node;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.util.DependencyInjectionUtil;
import scraper.util.JobUtil;
import scraper.utils.ClassUtil;

import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.security.Permission;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static scraper.util.NodeUtil.addressOf;
import static scraper.util.NodeUtil.flowOf;

public class AddressTest {


    @Test
    public void instanceAddressEqualityTest() {
        Assert.assertEquals( new InstanceAddressImpl("debug"), addressOf("debug") );
        Assert.assertEquals( addressOf("debug"), new InstanceAddressImpl("debug") );

        Assert.assertEquals( new InstanceAddressImpl("debug").hashCode(), addressOf("debug").hashCode() );
    }

    @Test
    public void graphAddressEqualityTest() {
        Assert.assertEquals( new GraphAddressImpl("debug", "graph"), addressOf("debug.graph") );
        Assert.assertEquals( addressOf("debug.graph"), new GraphAddressImpl("debug", "graph") );

        Assert.assertEquals( addressOf("debug.graph").hashCode(), new GraphAddressImpl("debug", "graph").hashCode() );
    }

    @Test
    public void nodeAddressIndexEqualityTest() {
        Assert.assertEquals(
                addressOf("debug.graph.2"),
                new NodeAddressImpl("debug", "graph", null, 2)
        );
        Assert.assertEquals(
                new NodeAddressImpl("debug", "graph", null, 2),
                addressOf("debug.graph.2")
        );

        Assert.assertEquals(
                addressOf("debug.graph.2").hashCode(),
                new NodeAddressImpl("debug", "graph", null, 2).hashCode()
        );
    }

    @Test
    public void nodeAddressLabelEqualityTest() {
        Assert.assertEquals(
                addressOf("debug.graph.node"),
                new NodeAddressImpl("debug", "graph", "node", null)
        );
        Assert.assertEquals(
                new NodeAddressImpl("debug", "graph", "node", null),
                addressOf("debug.graph.node")
        );

        Assert.assertEquals(
                new NodeAddressImpl("debug", "graph", "node", null).hashCode(),
                addressOf("debug.graph.node").hashCode()
        );
    }

    @Test
    public void nodeAddressSymmetryIndexEqualityTest() {
        Assert.assertEquals(
                addressOf("debug.graph.0"),
                new NodeAddressImpl("debug", "graph", "node", 0)
        );
        Assert.assertEquals(
                new NodeAddressImpl("debug", "graph", "node", 0),
                addressOf("debug.graph.0")
        );

        Assert.assertEquals(
                new NodeAddressImpl("debug", "graph", "node", null).hashCode(),
                addressOf("debug.graph.node").hashCode()
        );
    }

    @Test
    public void nodeAddressSymmetryLabelEqualityTest() {
        Assert.assertEquals(
                addressOf("debug.graph.node"),
                new NodeAddressImpl("debug", "graph", "node", 0)
        );
        Assert.assertEquals(
                new NodeAddressImpl("debug", "graph", "node", 0),
                addressOf("debug.graph.node")
        );

        Assert.assertEquals(
                new NodeAddressImpl("debug", "graph", "node", 0).hashCode(),
                addressOf("debug.graph.node").hashCode()
        );
    }

    // reflexivity
    @Test
    public void addressReflexivityTest() {
        Assert.assertEquals(
                addressOf("debug.graph.2"),
                addressOf("debug.graph.2")
        );
        Assert.assertEquals(
                addressOf("debug.graph.node"),
                addressOf("debug.graph.node")
        );

        Assert.assertEquals(
                addressOf("debug.graph.2").hashCode(),
                addressOf("debug.graph.2").hashCode()
        );
    }

    @Test
    public void graphAddressReflexivityTest() {
        Assert.assertEquals(
                new GraphAddressImpl("debug", "graph"),
                new GraphAddressImpl("debug", "graph")
        );

        Assert.assertEquals(
                new GraphAddressImpl("debug", "graph").hashCode(),
                new GraphAddressImpl("debug", "graph").hashCode()
        );
    }

    @Test
    public void instanceAddressReflexivityTest() {
        Assert.assertEquals(
                new InstanceAddressImpl("debug"),
                new InstanceAddressImpl("debug")
        );

        Assert.assertEquals(
                new InstanceAddressImpl("debug").hashCode(),
                new InstanceAddressImpl("debug").hashCode()
        );
    }

    // label and index together is only allowed for NodeAddress
    @Test(expected = Exception.class)
    public void forbidInferringLabelAndIndexRelationTest() {
        addressOf("debug.graph.node:0");
    }

}