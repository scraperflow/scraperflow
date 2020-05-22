package scraper.plugins.core.flowgraph;

import scraper.annotations.node.FlowKey;
import scraper.api.node.Address;
import scraper.api.node.container.NodeContainer;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.DefaultVisitor;
import scraper.api.template.T;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;
import scraper.util.NodeUtil;
import scraper.utils.ClassUtil;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Typer {

    static void analyze(ScrapeInstance job) {
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job, true);
        cfg.getNodes().forEach((adr, node) -> analyzeNode(job, cfg, adr, node));
    }

    private static void analyzeNode(ScrapeInstance job, ControlFlowGraph cfg, Address adr, ControlFlowNode cnode) {
        NodeContainer<?> node = job.getNode(adr).get();
        Map<String, T<?>> templates = getDefaultDataFlowInput(node);
        templates.forEach((name, token) -> analyzeTemplate(name, token, job, cfg, adr, cnode));
    }

    private static void analyzeTemplate(String name, T<?> token, ScrapeInstance job, ControlFlowGraph cfg, Address adr, ControlFlowNode cnode) {
        // TODO
        token.getTerm().accept( new DefaultVisitor(){ });
    }


    private static Map<String, T<?>> getDefaultDataFlowInput(NodeContainer<?> node) {
        List<Field> inputData = ClassUtil.getAllFields(new LinkedList<>(), node.getC().getClass()).stream()
                // only templates
                .filter(field -> field.getType().isAssignableFrom(T.class))
                // only annotated by flow keys
                .filter(field -> field.getAnnotation(FlowKey.class) != null)
                .collect(Collectors.toList());
        return NodeUtil.extractInputFromFields(inputData, node);
    }
}
