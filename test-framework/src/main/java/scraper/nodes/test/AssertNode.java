package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.template.T;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.valueOf;
import static scraper.api.node.container.NodeLogLevel.ERROR;
import static scraper.api.node.container.NodeLogLevel.INFO;

@NodePlugin
public class AssertNode implements Node {

    private @FlowKey(defaultValue = "{}")
    T<Map<String, Object>> assertMap = new T<>(){};
    private @FlowKey(defaultValue = "{}")
    T<Map<String, List<String>>> containsMap = new T<>(){};
    private @FlowKey(defaultValue = "{}")
    T<Map<String, List<String>>> containedInMap = new T<>(){};
    private @FlowKey(defaultValue = "[]")
    T<List<List<String>>> mathCompare = new T<>(){};
    private @FlowKey(defaultValue = "false") Boolean negate;
    private @FlowKey(defaultValue = "false") Boolean failOnError;


    private final AtomicBoolean success = new AtomicBoolean(true);
    public AtomicBoolean getSuccess() { return success; }


    private boolean finished = false;
    public boolean isFinished() { return finished; }


    private @FlowKey
    Integer wait;

    @NotNull
    @Override
    public FlowMap process(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) throws NodeException {
        if(wait != null) { try { Thread.sleep(wait); } catch (InterruptedException ignored) {} }

        Map<String, Object> assertMap = o.evalIdentity(this.assertMap);
        Map<String, List<String>> containsMap  = o.evalIdentity(this.containsMap);
        Map<String, List<String>> containedInMap  = o.evalIdentity(this.containedInMap);
        List<List<String>> mathCompare  = o.evalIdentity(this.mathCompare);

        for (String key : assertMap.keySet()) {
            n.log(INFO,"Assert @{0}: {1}{2}{3}", key, clip(valueOf(o.get(key))), (negate?" != ":" == "), assertMap.get(key));

            // assert does not work for Numbers (e.g. 1 == 1L) , use BigDecimal wrapper
            if(o.get(key).isPresent() && Number.class.isAssignableFrom(o.get(key).get().getClass())
                    && assertMap.get(key) != null && Number.class.isAssignableFrom(assertMap.get(key).getClass())) {
                if(!(new BigDecimal(valueOf(assertMap.get(key))).equals(new BigDecimal(valueOf(o.get(key).get()))))) {
                    if(!negate) {
                        n.log(ERROR, "Assertion wrong: {0} != {1}", assertMap.get(key), clip(valueOf(o.get(key).get())));
                        success.set(false);
                    }
                } else {
                    if(negate) {
                        n.log(ERROR, "Equals but not expected: {0} == {1}", assertMap.get(key), clip(valueOf(o.get(key).get())));
                        success.set(false);
                    }
                }
            }
            else if(!assertMap.get(key).equals(o.get(key).get())) {
                if(!negate) {
                    n.log(ERROR, "Assertion wrong: {0} != {1}", assertMap.get(key), clip(valueOf(o.get(key).get())));
                    success.set(false);
                }
            } else {
                if(negate) {
                    n.log(ERROR, "Assertion true, but not expected: {0} == {1}", assertMap.get(key), clip(valueOf(o.get(key).get())));
                    success.set(false);
                }
            }
        }

        for (String key : containsMap.keySet()) {
            assert o.get(key).isPresent();
            n.log(INFO,"Assert contains: @{0} contains {1}", key, clip(valueOf(o.get(key).get())));
            String actual = valueOf(o.get(key).get());
            List<String> expected = containsMap.get(key);

            for (String s : expected) {
                if(!actual.contains(s)) {
                    if(!negate) {
                        n.log(ERROR, "Expected not contained in actual: {0}", s);
                        success.set(false);
                    }
                } else {
                    if(negate) {
                        n.log(ERROR, "Expected contained in actual, but not expected: {0}", s);
                        success.set(false);
                    }
                }
            }
        }

        for (String key : containedInMap.keySet()) {
            String actual = valueOf(o.get(key).get());
            n.log(INFO,"Assert contained in @{0}: {1}", key, actual);
            List<String> contained = containedInMap.get(key);

            if(!contained.contains(actual)) {
                if(!negate) {
                    n.log(ERROR, "Actual not contained in expected set: {0}", actual);
                    success.set(false);
                }
            } else {
                if(negate) {
                    n.log(ERROR, "Actual contained in set but not expected: {1}", actual);
                    success.set(false);
                }
            }
        }

        for (List<String> eq : mathCompare) {
            formula(n, eq, o);
        }

        finished = true;
        synchronized (success) {
            success.notifyAll();
        }

        if (failOnError && !success.get()) throw new NodeException("Assertion error!");

        return o;
    }

    private String clip(String o) {
        if(o.length() > 30) return o.substring(0,30);

        return o;
    }

    private void formula(NodeContainer<?> n, List<String> eq, FlowMap o) {
        String left = eq.get(0);
        if(left.startsWith("@")) left = valueOf(o.get(left.substring(1)).get());
        String right = eq.get(2);
        if(right.startsWith("@")) right = valueOf(o.get(right.substring(1)).get());

        String op = eq.get(1);

        n.log(INFO, "Assert formula: {0}{1}{2}", left, op, right);
        switch (op) {
            case "<": {
                if(!(Integer.parseInt(left) < Integer.parseInt(right))) {
                    n.log(ERROR, "{0} not smaller than {1}", left, right);
                    success.set(false);
                }
                return;
            }
            case ">": {
                if(!(Integer.parseInt(left) > Integer.parseInt(right))) {
                    n.log(ERROR, "{0} not bigger than {1}", left, right);
                    success.set(false);
                }
                return;
            }
            case "==": {
                if(!(Integer.parseInt(left) == Integer.parseInt(right))) {
                    n.log(ERROR, "{0} not equal {1}", left, right);
                    success.set(false);
                }
                return;
            }
            case "<=": {
                if(!(Integer.parseInt(left) <= Integer.parseInt(right))) {
                    n.log(ERROR, "{0} not loe {1}", left, right);
                    success.set(false);
                }
                return;
            }
            case ">=": {
                if(!(Integer.parseInt(left) >= Integer.parseInt(right))) {
                    n.log(ERROR, "{0} not goe {1}", left, right);
                    success.set(false);
                }
            }
        }
    }
}
