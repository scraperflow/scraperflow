package scraper.plugins.core.pointerfree;

import scraper.annotations.FlowKey;
import scraper.api.*;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class PointerFreeMutator {

    private static final AtomicInteger counter = new AtomicInteger();

    public void mutateTaskFlow(ScrapeInstance job, ScrapeSpecification def, ControlFlowGraph cfg) {
        cfg.getEdges().forEach(e -> handleEdge(job, def, e));
    }

    private void handleEdge(ScrapeInstance job, ScrapeSpecification def, ControlFlowEdge e) {
        try {
            NodeContainer<? extends Node> outputNode = job.getNode(e.getFromAddress());
            NodeContainer<? extends Node> inputNode = job.getNode(e.getToAddress());

            List<Field> toAssignOutput = new LinkedList<>();
            List<Field> toAssignOutputMaybe = new LinkedList<>();
            for (Field f : outputNode.getC().getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(outputNode.getC());
                if(L.class.isAssignableFrom(f.getType())) {
                    if(!((L<?>) val).isAssigned()) {
                        if (f.getAnnotation(FlowKey.class).mandatory()) {
                            toAssignOutput.add(f);
                        }
                    } else {
                        if (((L<?>) val).getLocation().getRaw().equals("_")) {
                            toAssignOutputMaybe.add(f);
                        }
                    }
                }
            }

            if(toAssignOutput.size() == 0 && toAssignOutputMaybe.size() == 0) return;
            if(toAssignOutput.size() > 1) return;

            List<Field> toAssignInput = new LinkedList<>();
            for (Field f : inputNode.getC().getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(inputNode.getC());
                if(T.class.isAssignableFrom(f.getType())) {
                    if(!((T<?>) val).isAssigned()) {
                        if (f.getAnnotation(FlowKey.class).mandatory()) {
                            toAssignInput.add(f);
                        }
                    }
                } else {
                    // TODO make everything a template?
                    if(!L.class.isAssignableFrom(f.getType())) {
                        if(val == null && f.getAnnotation(FlowKey.class).mandatory()) {
                            toAssignInput.add(f);
                        }
                    }
                }
            }

            if(toAssignInput.size() == 0) return;
            if(toAssignInput.size() > 1) return;

            Field input = toAssignInput.get(0);
            Field output;
            if(toAssignOutput.isEmpty()) {
                output = toAssignOutputMaybe.get(0);
            } else {
                output = toAssignOutput.get(0);
            }


            int count = counter.getAndIncrement();

            Map<String, Object> changeOutputNode = def.getGraphs().get(e.getFromAddress().getGraph()).get(e.getFromAddress().getIndex());
            changeOutputNode.put(output.getName(), "var$"+count);

            Map<String, Object> changeInputNode = def.getGraphs().get(e.getToAddress().getGraph()).get(e.getToAddress().getIndex());
            changeInputNode.put(input.getName(), "{var$"+count+"}");
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }

    }
}
