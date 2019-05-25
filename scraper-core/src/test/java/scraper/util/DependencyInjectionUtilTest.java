package scraper.util;

import org.junit.Assert;
import org.junit.Test;
import scraper.addons.TestAddon;
import scraper.annotations.di.DITarget;
import scraper.api.di.DIContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.hooks.ExitHook;
import scraper.hooks.NodeDependencyGeneratorHook;
import scraper.hooks.TestHook;
import scraper.hooks.TestPreHook;
import scraper.hooks.PluginHook;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyInjectionUtilTest {

    // Testing addons, hooks, and plugins discovery
    @Test
    public void getDIContainer() throws Exception {
        DIContainer container = DependencyInjectionUtil.getDIContainer();

        // addon discovery
        container.get(TestAddon.class).load(container);

        // hook discovery
        container.get(TestHook.class).execute(container, null, null);

        // pre hook discovery
        container.get(TestPreHook.class).execute(container, null);

        // plugin discovery
        container.get(PluginHook.class).execute(container, null, null);

        Assert.assertEquals("true", System.getProperty("test-addon"));
        Assert.assertEquals("true", System.getProperty("test-hook"));
        Assert.assertEquals("true", System.getProperty("test-pre-hook"));
        Assert.assertEquals("true", System.getProperty("plugin-hook"));

        // exit hook
        container.get(ExitHook.class).execute(container, new String[]{"exit"}, Map.of());
        Assert.assertEquals("true", System.getProperty("scraper.exit"));


        // ndep hook
        File temp = File.createTempFile("ndep-test", null).getParentFile();
        JobFactory jobFactory = container.get(JobFactory.class);
        List<ScrapeSpecification> specs = YmlParse.parseYmlFile(getClass().getResource("yml/minimal.yml").getPath(), Set.of());
        ScrapeSpecification spec = specs.get(0);
        ScrapeInstance instance = jobFactory.convertScrapeJob(spec);
        container.get(NodeDependencyGeneratorHook.class).execute(container, new String[]{"ndep:"+temp.getPath()}, Map.of(spec, instance));
        Path path = Path.of(temp.getPath(), "job1.ndep");
        File ndep = new File(path.toString());
        Assert.assertTrue(ndep.exists());
        ndep.deleteOnExit();
        temp.deleteOnExit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingAnnotationForCollectionTest() {
        DIContainer container = DependencyInjectionUtil.getDIContainer();
        container.addComponent(ClassWithCollectionDependency.class);
    }

    @Test
    public void collectionDependencyTest() {
        DIContainer container = DependencyInjectionUtil.getDIContainer();
        container.addComponent(ClassWithCollectionDependencyOk.class);

        ClassWithCollectionDependencyOk inst = container.get(ClassWithCollectionDependencyOk.class);
        Assert.assertEquals(0, inst.field.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addingSameClassTwiceTest() {
        DIContainer container = DependencyInjectionUtil.getDIContainer();
        container.addComponent(ClassWithCollectionDependencyOk.class);
        container.addComponent(ClassWithCollectionDependencyOk.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonAccessibleTest() {
        DIContainer container = DependencyInjectionUtil.getDIContainer();
        container.addComponent(NonPublicClass.class);
    }

    @Test
    public void waitingForDependenciesTest() {
        DIContainer container = DependencyInjectionUtil.getDIContainer();
        container.addComponent(CDepdendsOnA.class);
        Assert.assertNull(container.get(CDepdendsOnA.class));

        container.addComponent(ADependsOnB.class);
        Assert.assertNull(container.get(CDepdendsOnA.class));
        Assert.assertNull(container.get(ADependsOnB.class));

        container.addComponent(B.class);
        Assert.assertNotNull(container.get(CDepdendsOnA.class));
        Assert.assertNotNull(container.get(ADependsOnB.class));
        Assert.assertNotNull(container.get(B.class));
    }

    static public class ClassWithCollectionDependency {
        public ClassWithCollectionDependency(@SuppressWarnings("unused") Collection<String> missingAnnotation) {}
    }

    static public class ClassWithCollectionDependencyOk {
        Collection<String> field;
        public ClassWithCollectionDependencyOk(@DITarget(String.class) Collection<String> missingAnnotation) {
            field = missingAnnotation;
        }
    }

    private static class NonPublicClass {}

    public static class B {
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean ok() {return true;}
    }
    public static class ADependsOnB {
        B b;
        public ADependsOnB(B dependency) {
            if(!dependency.ok()) throw new IllegalArgumentException("This is not ok");
            this.b = dependency;
        }
    }

    public static class CDepdendsOnA {
        public CDepdendsOnA(ADependsOnB dependency, B b) {
            if(!dependency.b.ok()) throw new IllegalArgumentException("This is not ok");
            if(!b.ok()) throw new IllegalArgumentException("This is not ok");
            if(b != dependency.b) throw new IllegalArgumentException("Wrong objects");
        }
    }
}