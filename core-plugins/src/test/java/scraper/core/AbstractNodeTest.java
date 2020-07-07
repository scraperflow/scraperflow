package scraper.core;

import org.junit.jupiter.api.Test;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.utils.ClassUtil;

import java.io.IOException;
import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.security.Permission;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static scraper.util.NodeUtil.addressOf;

@SuppressWarnings("rawtypes") // testing abstract node
public class AbstractNodeTest {

    private ScrapeInstaceImpl getInstance(String base, String file) throws IOException, ValidationException {
        URL baseurl = getClass().getResource(base);
        return InstanceHelper.getInstance(baseurl, file);
    }

    @Test
    public void simpleFunctionalNodeTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "job2.jf");

        NodeContainer<? extends Node> node = opt(instance);
        assertTrue(node instanceof AbstractFunctionalNode);

        FlowMapImpl o = (FlowMapImpl) FlowMapImpl.origin();
        o = (FlowMapImpl) node.getC().accept(node, o);

        assertNotNull(o.getPrivateMap().get("simple"));
        assertEquals(true, o.getPrivateMap().get("simple"));

        AbstractNode abstractNode = (AbstractNode) node;
        assertNotNull(abstractNode.getL());
        assertEquals(NodeLogLevel.INFO, abstractNode.getLogLevel());
        assertEquals("SimpleFunctionalNode", abstractNode.getType());
    }

    @Test
    public void globalNodeConfigurationsTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "all-field.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMapImpl o = (FlowMapImpl) FlowMapImpl.origin();
        o = (FlowMapImpl) node.getC().accept(node, o);

        assertNull(o.getPrivateMap().get("simple"));
        // local key overwrites all key
        assertNotNull(o.getPrivateMap().get("overwritten"));
        assertNotNull(o.getPrivateMap().get("goTo"));
        assertEquals(true, o.getPrivateMap().get("overwritten"));
        assertEquals(true, o.getPrivateMap().get("goTo"));
    }


    @Test
    public void getService() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "dummy.jf");

        AbstractNode<? extends Node> node = (AbstractNode<? extends Node>) opt(instance);
        node.service = "newService";

        // service thread group is created only once
        assertEquals(node.getService(), node.getService());

        AtomicBoolean t = new AtomicBoolean(false);
        node.dispatch(() -> { t.set(true); return null; });
        Thread.sleep(50);
        assertTrue(t.get());
    }

    @Test
    public void badJsonDefaultTest() throws Exception {
        assertThrows(ValidationException.class, () -> {
            getInstance("abstract", "bad-node.jf");
        });
    }

    @Test
    public void badLogTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "bad-log.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMap o = FlowMapImpl.origin();
        // bad log should never stop the process
        ((AbstractNode<?>) node).start(node, o);
    }

    @Test
    public void fileTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-node.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMap o = FlowMapImpl.origin();
        ((AbstractNode<?>) node).start(node, o);
    }

    @Test
    public void fileWithTemplateTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-template.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMap o = FlowMapImpl.origin();
        o.output("path-template", "/tmp/scraper-ok");
        ((AbstractNode<?>) node).start(node, o);
    }

    @Test
    public void allLogLevelsTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "log-levels.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMap o = FlowMapImpl.origin();

        //trace
        node.getC().accept(node, o);

        assertTrue(node.getKeySpec("logLevel").isPresent());
        assertEquals("TRACE", node.getKeySpec("logLevel").get());

    }


    @Test
    public void goodListGoToTest() throws Exception {
        getInstance("abstract", "good-list-goto.jf");
    }

    @Test
    public void goToNodeTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "goto.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMap o = FlowMapImpl.origin();
        FlowMap o2 = node.forward(o);

        FlowMapImpl o3 = (FlowMapImpl) node.eval(o2, addressOf("oor"));
        assertNotNull(o3.getPrivateMap().get("y"));
    }

    @Test
    public void impliedGoToTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "implied-goto.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMap o = FlowMapImpl.origin();
        FlowMapImpl o2 = (FlowMapImpl) node.forward(o);

        assertEquals(1, o2.getPrivateMap().size());
    }

    @Test
    public void functionalNodeWithGotoTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "func-goto.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMapImpl o = (FlowMapImpl) FlowMapImpl.origin();
        o = (FlowMapImpl) node.forward(o);

        assertNotNull(o.getPrivateMap().get("simple"));
        assertEquals(true, o.getPrivateMap().get("simple"));
    }

    // inject IllegalAccessException on reflection access to get code coverage
    @Test
    public void reflectionCodeCoverageTest() throws Exception {
        assertThrows(ValidationException.class, () -> {
            SecurityManager sm = System.getSecurityManager();
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    if (perm instanceof ReflectPermission && "suppressAccessChecks".equals(perm.getName())) {
                        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                            if ("scraper.util.NodeUtil".equals(elem.getClassName()) && "initField".equals(elem.getMethodName())) {
                                ClassUtil.sneakyThrow(new IllegalAccessException("Illegal Access!"));
                            }
                        }
                    }
                }
            });

            try {
                getInstance("abstract", "func-goto.jf");
            } finally {
                System.setSecurityManager(sm);
            }
        });
    }

    // inject IllegalAccessException on reflection access to get code coverage
    @Test
    public void reflectionCodeCoverageTestStart() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            SecurityManager sm = System.getSecurityManager();

            try {
                ScrapeInstaceImpl instance = getInstance("abstract", "file-node.jf");

                System.setSecurityManager(new SecurityManager() {
                    @Override
                    public void checkPermission(Permission perm) {
                        if (perm instanceof ReflectPermission && "suppressAccessChecks".equals(perm.getName())) {
                            for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                                if ("scraper.core.AbstractNode".equals(elem.getClassName()) && "start".equals(elem.getMethodName())) {
                                    ClassUtil.sneakyThrow(new IllegalAccessException("Illegal Access!"));
                                }
                            }
                        }
                    }
                });

                NodeContainer<? extends Node> node = opt(instance);
                FlowMap o = FlowMapImpl.origin();
                ((AbstractNode<?>) node).start(node, o);
            } finally {
                System.setSecurityManager(sm);
            }
        });
    }

    @Test
    public void badEnsureFileTest() throws Exception {
        assertThrows(NodeException.class, () -> {
            ScrapeInstaceImpl instance = getInstance("abstract", "file-bad-node.jf");

            NodeContainer<? extends Node> node = opt(instance);
            FlowMap o = FlowMapImpl.origin();
            node.getC().accept(node, o);
        });
    }

    @Test
    public void nullEnsureFileTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-notwanted.jf");

        NodeContainer<? extends Node> node = opt(instance);
        FlowMap o = FlowMapImpl.origin();
        node.getC().accept(node, o);
    }

    @Test
    public void badNodeTest() throws Exception {
        assertThrows(ValidationException.class, () -> {
            getInstance("abstract", "bad-node-2.jf");
        });
    }


    @Test
    public void indexAndLabelTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");
        NodeContainer<? extends Node> node = opt(instance);
        assertEquals(addressOf("debug.start.startingnode"), node.getAddress());
    }

    @Test
    public void onlyIndexTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");

        Optional<NodeContainer<? extends Node>> node = instance.getNode(addressOf("debug.start.1"));
        assertTrue(node.isPresent());
        assertEquals("<debug.start.1>", node.get().getAddress().toString());
    }

    @Test
    public void secondGraphMixedTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");

        // address target -> address expected output
        Map.of("0", "<debug.testing.0>",
                "hellonode", "<debug.testing.hellonode:1>",
                "1", "<debug.testing.hellonode:1>",
                "2", "<debug.testing.2>"
        ).forEach((target, expected) -> {
            assertTrue(instance.getNode(addressOf("debug.testing."+target)).isPresent());
            assertEquals(expected, instance.getNode(addressOf("debug.testing."+target)).get().toString());
        });
    }

    @Test
    public void graphAddressTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");
        // address target -> address expected output
        Map.of("testing", "<debug.testing.0>",
                "start", "<debug.start.startingnode:0>"
        ).forEach((target, expected) -> {
            assertTrue(instance.getNode(addressOf("debug."+target)).isPresent());
            assertEquals(expected, instance.getNode(addressOf("debug."+target)).get().toString());
        });
    }


    @Test
    public void instanceAddressTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");
        // address target -> address expected output
        Map.of("debug", "<debug.start.startingnode:0>" )
                .forEach((target, expected) -> {
            assertTrue(instance.getNode(addressOf(target)).isPresent());
            assertEquals(expected, instance.getNode(addressOf(target)).get().toString());
        });
    }

    @Test
    public void tooManyKeysTest() throws Exception {
        assertThrows(ValidationException.class, () -> {
            getInstance("abstract", "field-missing-only-warning.jf");
        });
    }

    @Test
    public void badGoToTest() throws Exception {
            assertThrows(ValidationException.class, () -> {
        getInstance("abstract", "bad-goto.jf");
            });
    }

    @Test
    public void badForwardTest() throws Exception {
                assertThrows(ValidationException.class, () -> {
        getInstance("abstract", "bad-forward.jf");
                });
    }

    @Test
    public void badListGoToTest() throws Exception {
                    assertThrows(ValidationException.class, () -> {
        getInstance("abstract", "bad-list-goto.jf");
                    });
    }

    @Test
    public void badFieldTest() throws Exception {
                        assertThrows(ValidationException.class, () -> {
        getInstance("abstract", "bad-field.jf");
                        });
    }

    private NodeContainer<? extends Node> opt(ScrapeInstance i) {
        Optional<NodeContainer<? extends Node>> e = i.getEntry();
        assertTrue(e.isPresent());
        return e.get();
    }
}