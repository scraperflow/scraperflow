package scraper.test.util;

import org.junit.jupiter.api.Test;
import scraper.test.addons.TestAddon;
import scraper.annotations.DITarget;
import scraper.api.DIContainer;
import scraper.test.hooks.PluginHook;
import scraper.test.hooks.TestHook;
import scraper.util.DependencyInjectionUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyInjectionUtilTest {

    // Testing addons, hooks, and plugins discovery
    @Test
    public void getDIContainer() throws Exception {
        DIContainer container = DependencyInjectionUtil.getDIContainer();

        // addon discovery
        Objects.requireNonNull(container.get(TestAddon.class)).load(container, new String[]{});

        // hook discovery
        Objects.requireNonNull(container.get(TestHook.class)).execute(container, new String[]{}, Map.of());

        // plugin discovery
        Objects.requireNonNull(container.get(PluginHook.class)).execute(container, new String[]{}, Map.of());

        assertEquals("true", System.getProperty("test-addon"));
        assertEquals("true", System.getProperty("test-hook"));
        assertEquals("true", System.getProperty("plugin-hook"));


        // ndep hook
        // FIXME yml file for groups of scrape jobs
//        File temp = File.createTempFile("ndep-test", null).getParentFile();
//        JobFactory jobFactory = container.get(JobFactory.class);
//        List<ScrapeSpecification> specs = YmlParse.parseYmlFile(getClass().getResource("yml/minimal.yml").getPath(), Set.of());
//        ScrapeSpecification spec = Objects.requireNonNull(specs).get(0);
//        ScrapeInstance instance = jobFactory.convertScrapeJob(spec);
//        container.get(NodeDependencyGeneratorHook.class).execute(container, new String[]{"ndep:"+temp.getPath()}, Map.of(spec, instance));
//        Path path = Path.of(temp.getPath(), "job1.ndep");
//        File ndep = new File(path.toString());
//        assertTrue(ndep.exists());
//        ndep.deleteOnExit();
//        temp.deleteOnExit();
//        assertTrue(ndep.delete());
//        container.get(NodeDependencyGeneratorHook.class).execute(container, new String[]{"ndep"}, Map.of(spec, instance));
//        URL impliedLocation = getClass().getResource("yml/job1.ndep");
//        assertNotNull(impliedLocation);
    }

    @Test
    public void missingAnnotationForCollectionTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            DIContainer container = DependencyInjectionUtil.getDIContainer();
            container.addComponent(ClassWithCollectionDependency.class);
        });
    }

    @Test
    public void collectionDependencyTest() {
        DIContainer container = DependencyInjectionUtil.getDIContainer();
        container.addComponent(ClassWithCollectionDependencyOk.class);

        ClassWithCollectionDependencyOk inst = container.get(ClassWithCollectionDependencyOk.class);
        assertEquals(0, Objects.requireNonNull(inst).field.size());
    }

    @Test
    public void addingSameClassTwiceTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            DIContainer container = DependencyInjectionUtil.getDIContainer();
            container.addComponent(ClassWithCollectionDependencyOk.class);
            container.addComponent(ClassWithCollectionDependencyOk.class);
        });
    }

    @Test
    public void nonAccessibleTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            DIContainer container = DependencyInjectionUtil.getDIContainer();
            container.addComponent(NonPublicClass.class);
        });
    }

    @Test
    public void waitingForDependenciesTest() {
        DIContainer container = DependencyInjectionUtil.getDIContainer();
        container.addComponent(CDepdendsOnA.class);
        assertNull(container.get(CDepdendsOnA.class));

        container.addComponent(ADependsOnB.class);
        assertNull(container.get(CDepdendsOnA.class));
        assertNull(container.get(ADependsOnB.class));

        container.addComponent(B.class);
        assertNotNull(container.get(CDepdendsOnA.class));
        assertNotNull(container.get(ADependsOnB.class));
        assertNotNull(container.get(B.class));
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