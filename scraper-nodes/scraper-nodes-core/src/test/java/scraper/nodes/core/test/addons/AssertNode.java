package scraper.nodes.core.test.addons;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.valueOf;
import static scraper.api.node.container.NodeLogLevel.INFO;

@NodePlugin
public class AssertNode implements Node {

    private @FlowKey(defaultValue = "{}") T<Map<String, Object>> assertMap = new T<>(){};
    private @FlowKey(defaultValue = "{}") T<Map<String, List<String>>> containsMap = new T<>(){};
    private @FlowKey(defaultValue = "{}") T<Map<String, List<String>>> containedInMap = new T<>(){};
    private @FlowKey(defaultValue = "[]") T<List<List<String>>> mathCompare = new T<>(){};
    private @FlowKey(defaultValue = "false") Boolean negate;
    private @FlowKey(defaultValue = "false") Boolean failOnError;


    private final AtomicBoolean success = new AtomicBoolean(true);
    public AtomicBoolean getSuccess() { return success; }


    private boolean finished = false;
    public boolean isFinished() { return finished; }


    private @FlowKey Integer wait;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        if(wait != null) { try { Thread.sleep(wait); } catch (InterruptedException ignored) {} }

        Map<String, Object> assertMap = o.evalIdentity(this.assertMap);
        Map<String, List<String>> containsMap  = o.evalIdentity(this.containsMap);
        Map<String, List<String>> containedInMap  = o.evalIdentity(this.containedInMap);
        List<List<String>> mathCompare  = o.evalIdentity(this.mathCompare);

        for (String key : assertMap.keySet()) {
//            l.info("Assert @{}: {}{}{}", (String) key, clip(valueOf(o.get(key))), (negate?" != ":" == "), assertMap.get(key));

            // assert does not work for Numbers (e.g. 1 == 1L) , use BigDecimal wrapper
            if(o.get(key) != null && Number.class.isAssignableFrom(o.get(key).getClass())
                    && assertMap.get(key) != null && Number.class.isAssignableFrom(assertMap.get(key).getClass())) {
                if(!(new BigDecimal(valueOf(assertMap.get(key))).equals(new BigDecimal(valueOf(o.get(key)))))) {
                    if(!negate) {
//                        l.error("Assertion wrong: {} != {}", assertMap.get(key), clip(valueOf(o.get(key))));
                        success.set(false);
                    }
                } else {
                    if(negate) {
//                        l.error("Equals but not expected: {} == {}", assertMap.get(key), clip(valueOf(o.get(key))));
                        success.set(false);
                    }
                }
            }
            else if(!assertMap.get(key).equals(o.get(key))) {
                if(!negate) {
//                    l.error("Assertion wrong: {} != {}", assertMap.get(key), clip(valueOf(o.get(key))));
                    success.set(false);
                }
            } else {
                if(negate) {
//                    l.error("Assertion true, but not expected: {} == {}", assertMap.get(key), clip(valueOf(o.get(key))));
                    success.set(false);
                }
            }
        }

        for (String key : containsMap.keySet()) {
//            n.log(NodeLogLevel.INFO,"Assert contains: @{} contains {}", key, clip(valueOf(o.get(key))));
            String actual = valueOf(o.get(key));
            List<String> expected = containsMap.get(key);

            for (String s : expected) {
                if(!actual.contains(s)) {
                    if(!negate) {
//                        l.error("Expected not contained in actual: {}", s);
                        success.set(false);
                    }
                } else {
                    if(negate) {
//                        l.error("Expected contained in actual, but not expected: {}", s);
                        success.set(false);
                    }
                }
            }
        }

        for (String key : containedInMap.keySet()) {
            String actual = valueOf(o.get(key));
            n.log(INFO,"Assert contained in @{}: {}", key, actual);
            List<String> contained = containedInMap.get(key);

            if(!contained.contains(actual)) {
                if(!negate) {
//                    l.error("Actual not contained in expected set: {}", actual);
                    success.set(false);
                }
            } else {
                if(negate) {
//                    l.error("Actual contained in set but not expected: {}", actual);
                    success.set(false);
                }
            }
        }

        for (List<String> eq : mathCompare) {
            formula(eq, o);
        }

        finished = true;
        synchronized (success) {
            success.notifyAll();
        }

        if (failOnError && !success.get()) throw new NodeException("Assertion error!");

        return n.forward(o);
    }

    private String clip(String o) {
        if(o.length() > 30) return o.substring(0,30);

        return o;
    }

    private void formula(List<String> eq, FlowMap o) {
        String left = eq.get(0);
        if(left.startsWith("@")) left = valueOf(o.get(left.substring(1)));
        String right = eq.get(2);
        if(right.startsWith("@")) right = valueOf(o.get(right.substring(1)));

        String op = eq.get(1);

//        l.info("Assert formula: {}{}{}", left, op, right);
        switch (op) {
            case "<": {
                if(!(Integer.parseInt(left) < Integer.parseInt(right))) {
//                    l.error("{} not smaller than {}", left, right);
                    success.set(false);
                }
                return;
            }
            case ">": {
                if(!(Integer.parseInt(left) > Integer.parseInt(right))) {
//                    l.error("{} not bigger than {}", left, right);
                    success.set(false);
                }
                return;
            }
            case "==": {
                if(!(Integer.parseInt(left) == Integer.parseInt(right))) {
//                    l.error("{} not equal {}", left, right);
                    success.set(false);
                }
                return;
            }
            case "<=": {
                if(!(Integer.parseInt(left) <= Integer.parseInt(right))) {
//                    l.error("{} not loe {}", left, right);
                    success.set(false);
                }
                return;
            }
            case ">=": {
                if(!(Integer.parseInt(left) >= Integer.parseInt(right))) {
//                    l.error("{} not goe {}", left, right);
                    success.set(false);
                }
            }
        }
    }
}
