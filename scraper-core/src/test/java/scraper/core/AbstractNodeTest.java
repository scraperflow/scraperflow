package scraper.core;

import org.junit.Assert;
import org.junit.Test;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.ControlFlow;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Node;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.util.DependencyInjectionUtil;
import scraper.util.JobUtil;
import scraper.util.NodeUtil;
import scraper.utils.ClassUtil;

import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.security.Permission;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static scraper.util.NodeUtil.graphAddressOf;

public class AbstractNodeTest {

    private final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    private ScrapeInstaceImpl getInstance(String basePath, String scrapeFile) throws Exception {
        URL base = getClass().getResource(basePath);
        ScrapeSpecificationImpl spec = (ScrapeSpecificationImpl) JobUtil.parseJobs(new String[]{scrapeFile}, Set.of(base.getFile())).get(0);
        return Objects.requireNonNull(deps.get(JobFactory.class)).convertScrapeJob(spec);
    }

    @Test
    public void simpleControlFlowTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("jobfactory/simplecontrolflow", "job1.jf");

        ControlFlow startNode = instance.getGraph(graphAddressOf("start")).get(0);
        assertEquals(1, startNode.getOutput().size());
        assertEquals(0, startNode.getInput().size());
        assertEquals("<hello@0>\\nSimpleNode", startNode.getDisplayName());

        ControlFlow endNode = instance.getGraph(graphAddressOf("start")).get(1);
        assertEquals(0, endNode.getOutput().size());
        assertEquals(1, endNode.getInput().size());

        // FIXME
//        assertEquals("hello", instance.getGraph(addressOf("start")).get(0).getAddress().getLabel());
    }

    @Test
    public void complexControlFlowTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("cf", "job1.jf");

        ControlFlow middleNode = instance.getEntryGraph().get(1);
        assertEquals(3, middleNode.getOutput().size());
        assertEquals(1, middleNode.getInput().size());

        boolean oneDispatch = false;
        boolean oneMulti = false;
        for (ControlFlowEdge edge : middleNode.getOutput()) {
            if(edge.isDispatched()) oneDispatch = true;
            if(edge.isMultiple()) oneMulti = true;

            Assert.assertTrue(edge.getDisplayLabel().contains("goTo") || edge.getDisplayLabel().contains("3"));
        }

        Assert.assertTrue(oneDispatch);
        Assert.assertTrue(oneMulti);
    }

    @Test
    public void simpleFunctionalNodeTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "job2.jf");

        Node node = instance.getEntryGraph().get(0);
        Assert.assertTrue(node instanceof AbstractFunctionalNode);

        FlowMap o = FlowMapImpl.of(Map.of());
        o = node.accept(o);

        assertEquals(true, o.get("simple"));

        AbstractNode abstractNode = ((AbstractNode) node);
        Assert.assertNotNull(abstractNode.getL());
        assertEquals(NodeLogLevel.INFO, abstractNode.getLogLevel());
        assertEquals("SimpleFunctionalNode", abstractNode.getType());
    }

    @Test
    public void globalNodeConfigurationsTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "all-field.jf");

        Node node = instance.getEntryGraph().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        o = node.accept(o);

        System.out.println(o);
        assertNull(o.get("simple"));
        // local key overwrites all key
        assertEquals(true, o.get("overwritten"));
        assertEquals(true, o.get("goTo"));
    }

    @Test
    public void tooManyKeysTest() throws Exception {
        // should not throw exception, only warning in the log that 'notexist' field is not expected
        getInstance("abstract", "field-missing-only-warning.jf");
    }

    @Test
    public void getService() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "dummy.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
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

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        // bad log should never stop the process
        node.start(o);
    }

    @Test
    public void fileTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-node.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        node.start(o);
    }

    @Test
    public void fileWithTemplateTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-template.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
        FlowMap o = FlowMapImpl.of(Map.of("path-template", "/tmp/scraper-ok"));
        node.start(o);
    }

    @Test
    public void allLogLevelsTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "log-levels.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
        FlowMap o = NodeUtil.flowOf((Map.of()));

        //trace
        node.start(o);
        node.accept(o);
        Assert.assertEquals("TRACE", node.getKeySpec("logLevel"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void badGoToTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "bad-goto.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0); FlowMap o = NodeUtil.flowOf((Map.of()));
        node.forward(o);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badForwardTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "bad-forward.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0); FlowMap o = NodeUtil.flowOf((Map.of()));
        node.forward(o);
    }

    @Test
    public void goToNodeTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "goto.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
        FlowMap o = NodeUtil.flowOf((Map.of()));
        FlowMap o2 = node.forward(o);

        FlowMap o3 = node.eval(o2, NodeUtil.addressOf("oor"));
        assertNotNull(o3.get("y"));
    }

    @Test
    public void impliedGoToTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "implied-goto.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
        FlowMap o = NodeUtil.flowOf((Map.of()));
        FlowMap o2 = node.forward(o);

        Assert.assertEquals(1, o2.size());
    }

    @Test
    public void functionalNodeWithGotoTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "func-goto.jf");

        Node node = instance.getEntryGraph().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        o = node.forward(o);

        assertEquals(true, o.get("simple"));
    }

    // inject IllegalAccessException on reflection access to get code coverage
    @Test(expected = ValidationException.class)
    public void reflectionCodeCoverageTest() throws Exception {
        SecurityManager sm = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                if (perm instanceof ReflectPermission && "suppressAccessChecks".equals(perm.getName())) {
                    for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                        if ("scraper.core.AbstractNode".equals(elem.getClassName()) && "initField".equals(elem.getMethodName())) {
                            ClassUtil.sneakyThrow(new IllegalAccessException());
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
    }

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

            AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
            FlowMap o = FlowMapImpl.of(Map.of());
            node.start(o);
        } finally {
            System.setSecurityManager(sm);
        }
    }

    @Test(expected = NodeException.class)
    public void badEnsureFileTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-bad-node.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        node.accept(o);
    }

    @Test
    public void nullEnsureFileTest() throws Exception {
        ScrapeInstaceImpl instance = getInstance("abstract", "file-notwanted.jf");

        AbstractNode node = (AbstractNode) instance.getEntryGraph().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        node.accept(o);
    }

    @Test(expected = ValidationException.class)
    public void badNodeTest() throws Exception {
        getInstance("abstract", "bad-node-2.jf");
    }
}