package scraper.nodes.test.helper;

import junitparams.Parameters;
import org.junit.Test;
import scraper.nodes.test.annotations.Functional;
import scraper.api.flow.FlowMap;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.Node;
import scraper.core.AbstractNode;
import scraper.util.NodeUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static scraper.util.NodeUtil.flowOf;

public abstract class FunctionalTest {

    protected Node node;

    @Test
    @Parameters
    public void functionalTests(
            String testCase,
            Class<?> nodeUnderTest,
            Class<?> expectException,
            Map<String, Object> spec,
            Map<String, Object> input,
            Map<String, Object> output
    ) throws Exception {
        // instantiate new node
        node = (Node) nodeUnderTest.getDeclaredConstructor().newInstance();
        Map<String,Object> specs = new HashMap<>(spec);
        specs.put("type", nodeUnderTest.getSimpleName());
        node.setNodeConfiguration(specs, NodeUtil.graphAddressOf("start"));

        try {

            // initialize node with mock scrape job instance
            ((AbstractNode) node).init(new MockInstance(node, specs));

            // feed input
            FlowMap actualOutput = flowOf(input);
            ((FunctionalNode) node).modify(actualOutput);

            FlowMap expectedOutput = flowOf(output);

            if (!actualOutput.containsElements(expectedOutput)) {
                throw new Exception("Map not matching: \nActual output: "+actualOutput+"\nExpected output: "+expectedOutput);
            }
        }
        // catch maybe expected exception
        catch (Exception e) {
            if(!expectException.isAssignableFrom(void.class)) {
                if(!e.getClass().isAssignableFrom(expectException)) {
                    System.err.println(testCase + " failed");
                    throw e;
                }
            } else {
                System.err.println(testCase + " failed");
                throw e;
            }
        }
    }

    // searches for Functional annotations
    protected Object[] parametersForFunctionalTests() throws InvocationTargetException, IllegalAccessException {
        List<Object[]> foundTestCases = new LinkedList<>();

        Class c = getClass();
        for (Method method : c.getDeclaredMethods()) {
            // found functional parameters
            if(method.isAnnotationPresent(Functional.class)) {
                Object[] params = (Object[]) method.invoke(this);

                // add goTo node class
                List<Object> merge = new LinkedList<>();
                merge.add(method.getName());
                merge.add(method.getAnnotation(Functional.class).value());
                merge.add(method.getAnnotation(Functional.class).expectException());
                merge.addAll(List.of(params));

                foundTestCases.add(merge.toArray());
            }
        }

        return foundTestCases.toArray(new Object[0]);
    }
}