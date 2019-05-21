package scraper.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import scraper.annotations.node.EnsureFile;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.ControlFlow;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Node;
import scraper.api.node.NodeInitializable;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.util.DependencyInjectionUtil;
import scraper.util.NodeUtil;
import scraper.utils.ClassUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.URL;
import java.security.Permission;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.Assert.*;

public class AbstractNodeTest {

    private final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    @Test
    public void simpleControlFlowTest() throws IOException, ValidationException {
        URL base = getClass().getResource("jobfactory");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("job1.scrape");
        specification.name("job1");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());

        ControlFlow startNode = instance.getJobProcess().get(0);
        assertEquals(1, startNode.getOutput().size());
        assertEquals(1, startNode.getInput().size());
        assertEquals("hello\\nSimpleNode@0", startNode.getName());

        ControlFlow endNode = instance.getJobProcess().get(1);
        assertEquals(0, endNode.getOutput().size());
        assertEquals(1, endNode.getInput().size());


        assertEquals("hello\\nSimpleNode@0", instance.getJobProcess().get(0).nameOf("hello"));
    }

    @Test
    public void complexControlFlowTest() throws IOException, ValidationException {
        URL base = getClass().getResource("cf");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("job1.scrape");
        specification.name("job1");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());

        ControlFlow middleNode = instance.getJobProcess().get(1);
        assertEquals(3, middleNode.getOutput().size());
        assertEquals(1, middleNode.getInput().size());

        boolean oneDispatch = false;
        boolean oneMulti = false;
        for (ControlFlowEdge edge : middleNode.getOutput()) {
            if(edge.isDispatched()) oneDispatch = true;
            if(edge.isMultiple()) oneMulti = true;


            Assert.assertTrue(edge.getLabel().contains("goto") || edge.getLabel().contains("3"));
            Assert.assertTrue(edge.getTargetLabel().contains("2") || edge.getTargetLabel().contains("target"));
        }

        Assert.assertTrue(oneDispatch);
        Assert.assertTrue(oneMulti);

    }

    @Test
    public void simpleFunctionalNodeTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("job2.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());

        Node node = instance.getJobProcess().get(0);
        Assert.assertTrue(node instanceof AbstractFunctionalNode);

        FlowMap o = FlowMapImpl.of(Map.of());
        node.accept(o);

        assertEquals(true, o.get("simple"));

        AbstractNode abstractNode = ((AbstractNode) node);
        Assert.assertNull(abstractNode.getFragment());
        assertEquals("functional", instance.getDescription());
        Assert.assertNotNull(abstractNode.getL());
        assertEquals("anode", abstractNode.get__comment());
        assertEquals(NodeLogLevel.INFO, abstractNode.getLogLevel());
        assertEquals("SimpleFunctionalNode", abstractNode.getType());
    }

    @Test
    public void allNodeTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("all-field.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());

        Node node = instance.getJobProcess().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        node.accept(o);

        assertNull(o.get("simple"));
        // local key overwrites all key
        assertEquals(true, o.get("overwritten"));
        assertEquals(true, o.get("target"));
    }

    @Test
    public void tooManyKeysTest() throws IOException, ValidationException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("field-missing-only-warning.scrape");
        JobFactory factory = deps.get(JobFactory.class);
        // should not throw exception, only warning in the log that 'notexist' field is not expected
        factory.convertScrapeJob(specification.build());
    }

    @Test
    public void getService() throws IOException, ValidationException, InterruptedException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("dummy.scrape");
        JobFactory factory = deps.get(JobFactory.class);
        // should not throw exception, only warning in the log that 'notexist' field is not expected
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);

        // service thread group is created only once
        assertEquals(node.getService("newService"), node.getService("newService", 300));
        assertEquals(node.getService("newService"), node.getService(null));

        AtomicBoolean t = new AtomicBoolean(false);
        node.dispatch(() -> { t.set(true); return null; });
        Thread.sleep(50);
    }

    @Test(expected = ValidationException.class)
    public void badJsonDefaultTest() throws IOException, ValidationException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("bad-node.scrape");
        JobFactory factory = deps.get(JobFactory.class);
        factory.convertScrapeJob(specification.build());
    }

    @Test
    public void badLogTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("bad-log.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        // bad log should never stop the process
        node.start(o);
    }

    @Test
    public void fileTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("file-node.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        node.start(o);
    }

    @Test
    public void fileWithTemplateTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("file-template.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        FlowMap o = FlowMapImpl.of(Map.of("path-template", "/tmp/scraper-ok"));
        node.start(o);
    }

    @Test
    public void allLogLevelsTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("log-levels.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        FlowMap o = NodeUtil.flowOf((Map.of()));

        //trace
        node.start(o);
        node.accept(o);
        Assert.assertEquals("TRACE", node.getKeySpec("logLevel"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void badGoToTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("bad-goto.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0); FlowMap o = NodeUtil.flowOf((Map.of()));
        node.goTo(o);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badForwardTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("bad-forward.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0); FlowMap o = NodeUtil.flowOf((Map.of()));
        node.forward(o);
    }

    @Test
    public void goToNodeTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("goto.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        FlowMap o = NodeUtil.flowOf((Map.of()));
        node.goTo(o);

        node.goTo(o, "oor");
        assertNotNull(o.get("y"));
    }

    @Test
    public void impliedGoToTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("implied-goto.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        FlowMap o = NodeUtil.flowOf((Map.of()));
        node.goTo(o);

        Assert.assertEquals(1, o.size());
    }

    @Test
    public void niyTest() throws IOException, ValidationException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("implied-goto.scrape");
        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());
        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        try {
            node.setArgument(null,null); fail();
        } catch (Exception ignored){}
        try {
            node.getNodeJsonSpec(); fail();
        } catch (Exception ignored){}
        try {
            node.setArgument(null, null); fail();
        } catch (Exception ignored){}
        try {
            node.setDefinition(null); fail();
        } catch (Exception ignored){}
        try {
            node.updateDefinition(); fail();
        } catch (Exception ignored){}
    }


    @Test
    public void functionalNodeWithGotoTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("func-goto.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());

        Node node = instance.getJobProcess().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        node.forward(o);

        assertEquals(true, o.get("simple"));
    }

    // inject IllegalAccessException on reflection access to get code coverage
    @Test(expected = ValidationException.class)
    public void reflectionCodeCoverageTest() throws IOException, ValidationException {
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
            URL base = getClass().getResource("abstract");
            ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
            specification.basePath(base.getPath());
            specification.scrapeFile("func-goto.scrape");

            JobFactory factory = deps.get(JobFactory.class);
            factory.convertScrapeJob(specification.build());
        } finally {
            System.setSecurityManager(sm);
        }
    }

    // inject IllegalAccessException on reflection access to get code coverage
    @Test(expected = RuntimeException.class)
    public void reflectionCodeCoverageTestStart() throws IOException, ValidationException, NodeException {
        SecurityManager sm = System.getSecurityManager();

        try {
            URL base = getClass().getResource("abstract");
            ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
            specification.basePath(base.getPath());
            specification.scrapeFile("file-node.scrape");

            JobFactory factory = deps.get(JobFactory.class);
            ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());

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

            AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
            FlowMap o = FlowMapImpl.of(Map.of());
            node.start(o);
        } finally {
            System.setSecurityManager(sm);
        }
    }

    @Test(expected = NodeException.class)
    public void badEnsureFileTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("file-bad-node.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());

        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        node.accept(o);
    }

    @Test
    public void nullEnsureFileTest() throws IOException, ValidationException, NodeException {
        URL base = getClass().getResource("abstract");
        ScrapeSpecificationImpl.JobDefinitionBuilder specification = new ScrapeSpecificationImpl.JobDefinitionBuilder();
        specification.basePath(base.getPath());
        specification.scrapeFile("file-notwanted.scrape");

        JobFactory factory = deps.get(JobFactory.class);
        ScrapeInstaceImpl instance = factory.convertScrapeJob(specification.build());

        AbstractNode node = (AbstractNode) instance.getJobProcess().get(0);
        FlowMap o = FlowMapImpl.of(Map.of());
        node.accept(o);
    }
}