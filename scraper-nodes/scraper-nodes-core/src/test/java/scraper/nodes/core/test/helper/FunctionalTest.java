package scraper.nodes.core.test.helper;

import junitparams.Parameters;
import org.junit.Test;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.nodes.core.test.annotations.Functional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static scraper.util.NodeUtil.flowOf;
import static scraper.util.NodeUtil.initFields;

public abstract class FunctionalTest {

    protected FunctionalNode node;

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
        node = (FunctionalNode) nodeUnderTest.getDeclaredConstructor().newInstance();


        Map<String,Object> specs = new HashMap<>(spec);
        specs.put("type", nodeUnderTest.getSimpleName());

        FunctionalNodeContainer mockContainer = new MockContainer(node, specs);
        MockInstance mockInstance = new MockInstance(mockContainer, spec);
//        node.setNodeConfiguration(specs, NodeUtil.graphAddressOf("start"));

        try {
            initFields(node, specs, input, Map.of());

            // initialize node with mock scrape job instance
            node.init(mockContainer, mockInstance);

            // feed input
            FlowMap actualOutput = FlowMapImpl.origin(input);
            node.modify(mockContainer, actualOutput);

            FlowMap expectedOutput = FlowMapImpl.origin(output);

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