package scraper.test;

import scraper.api.DIContainer;
import scraper.api.FlowMap;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.ScrapeSpecificationParser;
import scraper.api.ScrapeSpecification;
import scraper.api.flow.impl.FlowMapImpl;
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

        try {
            ScrapeInstaceImpl convJob = deps.get(JobFactory.class).convertScrapeJob(spec);
            convJob.validate();

            // build initial input map
            FlowMap initialFlow = FlowMapImpl.origin();

            // feed input
            Optional<NodeContainer<? extends Node>> n = convJob.getEntry();

            if(n.isEmpty()) throw new IllegalArgumentException("Job has no entry node");

            n.get().getC().accept(n.get(), initialFlow);

            TestUtil.assertSuccess(convJob.getAllNodes());
        } catch (Exception e) {
            if (!expectException.equals(e.getClass())) throw new RuntimeException(e);
        }
    }

    private static void injectExceptionNode(ScrapeSpecification spec) {
        spec.getGraphs().put("fail", List.of(Map.of("type", "Exception", "exception", "FAIL")));
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

}