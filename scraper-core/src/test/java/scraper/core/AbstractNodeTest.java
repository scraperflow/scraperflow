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

import static org.junit.Assert.*;
import static scraper.util.NodeUtil.addressOf;
import static scraper.util.NodeUtil.flowOf;

public class AbstractNodeTest {

    private final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    private ScrapeInstaceImpl getInstance(String basePath, String scrapeFile) throws Exception {
        URL base = getClass().getResource(basePath);
        ScrapeSpecificationImpl spec = (ScrapeSpecificationImpl) JobUtil.parseJobs(new String[]{scrapeFile}, Set.of(base.getFile())).get(0);
        return Objects.requireNonNull(deps.get(JobFactory.class)).convertScrapeJob(spec);
    }

    @Test
    public void simpleFunctionalNodeTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "job2.jf");

        NodeContainer<? extends Node> node = instance.getEntry();
        Assert.assertTrue(node instanceof AbstractFunctionalNode);

        FlowMap o = FlowMapImpl.of(Map.of());
        o = node.getC().accept(node, o);

        assertTrue(o.get("simple").isPresent());
        assertEquals(true, o.get("simple").get());

        AbstractNode abstractNode = ((AbstractNode) node);
        Assert.assertNotNull(abstractNode.getL());
        assertEquals(NodeLogLevel.INFO, abstractNode.getLogLevel());
        assertEquals("SimpleFunctionalNode", abstractNode.getType());
    }

    @Test
    public void globalNodeConfigurationsTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "all-field.jf");

        NodeContainer<? extends Node> node = instance.getEntry();
        FlowMap o = FlowMapImpl.of(Map.of());
        o = node.getC().accept(node, o);

        assertTrue(o.get("simple").isEmpty());
        // local key overwrites all key
        assertTrue(o.get("overwritten").isPresent());
        assertTrue(o.get("goTo").isPresent());
        assertEquals(true, o.get("overwritten").get());
        assertEquals(true, o.get("goTo").get());
    }


    @Test
    public void getService() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "dummy.jf");

        AbstractNode node = (AbstractNode) instance.getEntry();
        node.service = "newService";

        // service thread group is created only once
        assertEquals(node.getService(), node.getService());

        AtomicBoolean t = new AtomicBoolean(false);
        node.dispatch(() -> { t.set(true); return null; });
        Thread.sleep(50);
        Assert.assertTrue(t.get());
    }

    @Test(expected = ValidationException.class)
    public void badJsonDefaultTest() throws Exception {
        getInstance("abstract", "bad-node.jf");
    }

    @Test
    public void badLogTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "bad-log.jf");

        AbstractNode node = (AbstractNode) instance.getEntry();
        FlowMap o = FlowMapImpl.of(Map.of());
        // bad log should never stop the process
        node.start(o);
    }

    @Test
    public void fileTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-node.jf");

        AbstractNode node = (AbstractNode) instance.getEntry();
        FlowMap o = FlowMapImpl.of(Map.of());
        node.start(o);
    }

    @Test
    public void fileWithTemplateTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-template.jf");

        AbstractNode node = (AbstractNode) instance.getEntry();
        FlowMap o = FlowMapImpl.of(Map.of("path-template", "/tmp/scraper-ok"));
        node.start(o);
    }

    @Test
    public void allLogLevelsTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "log-levels.jf");

        NodeContainer<? extends Node> node = instance.getEntry();
        FlowMap o = flowOf((Map.of()));

        //trace
        node.getC().accept(node, o);

        Assert.assertTrue(node.getKeySpec("logLevel").isPresent());
        Assert.assertEquals("TRACE", node.getKeySpec("logLevel").get());

    }


    @Test
    public void goodListGoToTest() throws Exception {
        getInstance("abstract", "good-list-goto.jf");
    }

    @Test
    public void goToNodeTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "goto.jf");

        AbstractNode node = (AbstractNode) instance.getEntry();
        FlowMap o = flowOf((Map.of()));
        FlowMap o2 = node.forward(o);

        FlowMap o3 = node.eval(o2, addressOf("oor"));
        assertNotNull(o3.get("y"));
    }

    @Test
    public void impliedGoToTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "implied-goto.jf");

        AbstractNode node = (AbstractNode) instance.getEntry();
        FlowMap o = flowOf((Map.of()));
        FlowMap o2 = node.forward(o);

        Assert.assertEquals(1, o2.size());
    }

    @Test
    public void functionalNodeWithGotoTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "func-goto.jf");

        NodeContainer<? extends Node> node = instance.getEntry();
        FlowMap o = FlowMapImpl.of(Map.of());
        o = node.forward(o);

        assertTrue(o.get("simple").isPresent());
        assertEquals(true, o.get("simple").get());
    }

    // inject IllegalAccessException on reflection access to get code coverage
    // FIXME deprecated after architecture change?
//    @Test(expected = ValidationException.class)
//    public void reflectionCodeCoverageTest() throws Exception {
//        SecurityManager sm = System.getSecurityManager();
//        System.setSecurityManager(new SecurityManager() {
//            @Override
//            public void checkPermission(Permission perm) {
//                if (perm instanceof ReflectPermission && "suppressAccessChecks".equals(perm.getName())) {
//                    for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
//                        if ("scraper.core.AbstractNode".equals(elem.getClassName()) && "initField".equals(elem.getMethodName())) {
//                            ClassUtil.sneakyThrow(new IllegalAccessException());
//                        }
//                    }
//                }
//            }
//        });
//
//        try {
//            getInstance("abstract", "func-goto.jf");
//        } finally {
//            System.setSecurityManager(sm);
//        }
//    }

    // inject IllegalAccessException on reflection access to get code coverage
    @Test(expected = RuntimeException.class)
    public void reflectionCodeCoverageTestStart() throws Exception {
        SecurityManager sm = System.getSecurityManager();

        try {
            ScrapeInstaceImpl instance = getInstance("abstract", "file-node.jf");

            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    if (perm instanceof ReflectPermission && "suppressAccessChecks".equals(perm.getName())) {
                        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                            if ("scraper.core.AbstractNode".equals(elem.getClassName()) && "start".equals(elem.getMethodName())) {
                                ClassUtil.sneakyThrow(new IllegalAccessException());
                            }
                        }
                    }
                }
            });

            AbstractNode node = (AbstractNode) instance.getEntry();
            FlowMap o = FlowMapImpl.of(Map.of());
            node.start(o);
        } finally {
            System.setSecurityManager(sm);
        }
    }

    @Test(expected = NodeException.class)
    public void badEnsureFileTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-bad-node.jf");

        NodeContainer<? extends Node> node = instance.getEntry();
        FlowMap o = FlowMapImpl.of(Map.of());
        node.getC().accept(node, o);
    }

    @Test
    public void nullEnsureFileTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-notwanted.jf");

        NodeContainer<? extends Node> node = instance.getEntry();
        FlowMap o = FlowMapImpl.of(Map.of());
        node.getC().accept(node, o);
    }

    @Test(expected = ValidationException.class)
    public void badNodeTest() throws Exception {
        getInstance("abstract", "bad-node-2.jf");
    }


    @Test
    public void indexAndLabelTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");
        NodeContainer<? extends Node> node = instance.getEntry();
        Assert.assertEquals(addressOf("debug.start.startingnode"), node.getAddress());
    }

    @Test
    public void onlyIndexTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");

        Optional<NodeContainer<? extends Node>> node = instance.getNode(addressOf("debug.start.1"));
        Assert.assertTrue(node.isPresent());
        Assert.assertEquals("<debug.start.1>", node.get().getAddress().toString());
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
            Assert.assertTrue(instance.getNode(addressOf("debug.testing."+target)).isPresent());
            Assert.assertEquals(expected, instance.getNode(addressOf("debug.testing."+target)).get().toString());
        });
    }

    @Test
    public void graphAddressTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");
        // address target -> address expected output
        Map.of("testing", "<debug.testing.0>",
                "start", "<debug.start.startingnode:0>"
        ).forEach((target, expected) -> {
            Assert.assertTrue(instance.getNode(addressOf("debug."+target)).isPresent());
            Assert.assertEquals(expected, instance.getNode(addressOf("debug."+target)).get().toString());
        });
    }


    @Test
    public void instanceAddressTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("addressing", "debug.yf");
        // address target -> address expected output
        Map.of("debug", "<debug.start.startingnode:0>" )
                .forEach((target, expected) -> {
            Assert.assertTrue(instance.getNode(addressOf(target)).isPresent());
            Assert.assertEquals(expected, instance.getNode(addressOf(target)).get().toString());
        });
    }

    @Test(expected = ValidationException.class)
    public void tooManyKeysTest() throws Exception {
        getInstance("abstract", "field-missing-only-warning.jf");
    }

    @Test(expected = ValidationException.class)
    public void badGoToTest() throws Exception {
        getInstance("abstract", "bad-goto.jf");
    }

    @Test(expected = ValidationException.class)
    public void badForwardTest() throws Exception {
        getInstance("abstract", "bad-forward.jf");
    }

    @Test(expected = ValidationException.class)
    public void badListGoToTest() throws Exception {
        getInstance("abstract", "bad-list-goto.jf");
    }

    @Test(expected = ValidationException.class)
    public void badFieldTest() throws Exception {
        getInstance("abstract", "bad-field.jf");
    }
}