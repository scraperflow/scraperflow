package scraper.test;

import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.util.NodeUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static scraper.util.NodeUtil.initFields;

public abstract class FunctionalTest {

    @SuppressWarnings("unchecked")
    public static void runWith(Class<?> nut, List<?> args) {
        runWith(nut, void.class,(Map<String, Object>) args.get(0)
                , (Map<String, Object>) args.get(1)
                , (Map<String, Object>) args.get(2));
    }

    @SuppressWarnings("unchecked")
    public static void runWith(Class<?> nut, List<?> args, Class<?> exception) {
        runWith(nut, exception,(Map<String, Object>) args.get(0)
                , (Map<String, Object>) args.get(1)
                , (Map<String, Object>) args.get(2));
    }

    public static void runWith(
            Class<?> nodeUnderTest,
            Class<?> expectException,
            Map<String, Object> spec,
            Map<String, Object> input,
            Map<String, Object> output
    ) {
        // instantiate new node
        FunctionalNode node;
        try { node = (FunctionalNode) nodeUnderTest.getDeclaredConstructor().newInstance(); }
        catch (Exception e) { throw new RuntimeException(e); }


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
                    System.err.println("failed");
                    throw new RuntimeException(e);
                }
            } else {
                System.err.println("failed");
                throw new RuntimeException(e);
            }
        }
    }
}