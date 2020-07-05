package scraper.test;

import scraper.api.di.DIContainer;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.plugin.ScrapeSpecificationParser;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.core.JobFactory;
import scraper.util.DependencyInjectionUtil;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class WorkflowTest {

    private static final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    public static void runWith(URL workflow) {
        runWith(workflow, void.class);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static void runWith(URL workflow, Class<?> exception) {
        if(workflow == null) throw new NullPointerException("Workflow null");

        ScrapeSpecification spec = null;
        for (ScrapeSpecificationParser p : deps.getCollection(ScrapeSpecificationParser.class)) {
            try {
                spec = p.parseSingle(new File(workflow.getFile())).get();
            } catch (Exception ignored){}
        }

        workflowTests(Objects.requireNonNull(spec), exception);
    }

    public static void workflowTests(
            ScrapeSpecification spec,
            Class<?> expectException
    ) {
        // add fail safe to every test
        // i.e. redirect fork exceptions to the graph key 'fail', which triggers a System.setProperty set of workflow.fail = FAIL
        spec.getGlobalNodeConfigurations().put("/.*Node/", Map.of("onForkException", "fail"));
        injectExceptionNode(spec);

        // reset property before test
        System.setProperty("workflow.fail", "no");

        try {
            ScrapeInstaceImpl convJob = deps.get(JobFactory.class).convertScrapeJob(spec);

            // build initial input map
            FlowMap initialFlow = FlowMapImpl.origin();

            // feed input
            Optional<NodeContainer<? extends Node>> n = convJob.getEntry();

            if(n.isEmpty()) throw new IllegalArgumentException("Job has no entry node");

            n.get().getC().accept(n.get(), initialFlow);

            TestUtil.assertSuccess(convJob.getAllNodes());
        } catch (Exception e) {
            if(expectException.equals(e.getClass())) {
                System.out.println("Excepted exception");
            } else throw new RuntimeException(e);
        }

        // System fail
        if(System.getProperty("workflow.fail","no").equalsIgnoreCase("fail")) {
            if(expectException.equals(IllegalStateException.class)) {
                System.out.println("Excepted exception");
            } else {
                throw new IllegalStateException("Failed workflow by system property");
            }
        }
    }

    private static void injectExceptionNode(ScrapeSpecification spec) {
        spec.getGraphs().put("fail", List.of(Map.of("type", "ExceptionNode", "exception", "FAIL")));
        spec.getImports().forEach((key, imp) -> injectExceptionNode(imp.getSpec()));
    }

//    // searches for Workflow annotations
//    @SuppressWarnings("unused") // runner
//    protected Object[] parametersForWorkflowTests() {
//        List<Object[]> foundTestCases = new LinkedList<>();
//
//        Class<?> c = getClass();
//        for (Method method : c.getDeclaredMethods()) {
//            // found workflow parameters
//            if(method.isAnnotationPresent(Workflow.class)) {
//
//                List<Object> merge = new LinkedList<>();
//
//                // get folder
//                merge.add(c.getSimpleName().substring(0, c.getSimpleName().length()-4).toLowerCase());
//
//                // get test case name
//                merge.add(method.getName().toLowerCase());
//
//                // extract parameters from annotation
//                Workflow args = method.getAnnotation(Workflow.class);
//                merge.add(args.value());
//                merge.add(args.argumentFiles());
//                merge.add(args.expectToFail());
//
//                // TODO expected output
//                merge.add(Map.of());
//
//                // add as test case
//                foundTestCases.add(merge.toArray());
//            }
//        }
//
//        return foundTestCases.toArray(new Object[0]);
//    }

    public static URL resource(Class<?> clazz, String s) {
        return clazz.getResource(s);
    }
}