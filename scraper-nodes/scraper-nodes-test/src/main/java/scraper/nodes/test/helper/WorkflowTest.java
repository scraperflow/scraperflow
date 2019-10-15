package scraper.nodes.test.helper;

import junitparams.Parameters;
import org.junit.Test;
import scraper.api.di.DIContainer;
import scraper.api.flow.FlowMap;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.api.specification.impl.ScrapeSpecificationImpl;
import scraper.core.JobFactory;
import scraper.nodes.test.annotations.Workflow;
import scraper.util.DependencyInjectionUtil;
import scraper.util.JobUtil;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static scraper.util.NodeUtil.flowOf;

public abstract class WorkflowTest {

    @Test
    @Parameters
    public void workflowTests(
            String testCategory,
            String testCase,
            String workflowSpecification,
            String[] argumentFiles,
            Class<?> expectException,
            Map<String, Object> expectedOutput
    ) throws Exception {
        String base = testCategory+"/"+testCase+"/";
        DIContainer dibean = DependencyInjectionUtil.getDIContainer();

        // convert job
        URL baseURL = getClass().getResource(base);
        if(baseURL == null) throw new IllegalStateException("Missing base folder: " + base);

        ScrapeSpecificationImpl spec = (ScrapeSpecificationImpl) JobUtil.parseJobs(new String[]{workflowSpecification.toLowerCase()}, Set.of(baseURL.getFile().toLowerCase())).get(0);
        ScrapeInstaceImpl convJob = dibean.get(JobFactory.class).convertScrapeJob(spec);

        // build initial input map
        FlowMap initialFlow = flowOf(Map.of());

        // feed input
        try {
            convJob.getEntryGraph().get(0).accept(initialFlow);

            TestUtil.assertSuccess(convJob.getEntryGraph());
        } catch (Exception e) {
            if(expectException.equals(e.getClass())) {
                System.err.println("SUCCESS");
            } else throw e;
        }
        // TODO compare actual output with expected output
//        if (!actualOutput.containsElements(expectedOutput)) {
//            throw new Exception("Map not matching");
//        }
    }

    // searches for Workflow annotations
    protected Object[] parametersForWorkflowTests() {
        List<Object[]> foundTestCases = new LinkedList<>();

        Class c = getClass();
        for (Method method : c.getDeclaredMethods()) {
            // found workflow parameters
            if(method.isAnnotationPresent(Workflow.class)) {

                List<Object> merge = new LinkedList<>();

                // get folder
                merge.add(c.getSimpleName().substring(0, c.getSimpleName().length()-4).toLowerCase());

                // get test case name
                merge.add(method.getName().toLowerCase());

                // extract parameters from annotation
                Workflow args = method.getAnnotation(Workflow.class);
                merge.add(args.value());
                merge.add(args.argumentFiles());
                merge.add(args.expectToFail());

                // TODO expected output
                merge.add(Map.of());

                // add as test case
                foundTestCases.add(merge.toArray());
            }
        }

        return foundTestCases.toArray(new Object[0]);
    }

}