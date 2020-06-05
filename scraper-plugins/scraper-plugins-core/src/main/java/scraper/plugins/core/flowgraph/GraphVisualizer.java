package scraper.plugins.core.flowgraph;

import org.slf4j.Logger;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.plugins.core.flowgraph.api.ControlFlowNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class GraphVisualizer {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GraphVisualizer.class);

    static Boolean includeInstance = true;
    static Boolean includeGraph = true;
    static Boolean includeNodeType = true;
    static Boolean includeNodeAddress = true;
    static Boolean absolute = false;

    public static void visualize(ScrapeInstance job, String createCF, boolean realControlflow) throws FileNotFoundException {
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job, realControlflow);

        System.out.println(cfg);
        String graph = visualize(cfg);

        log.info("Output: {}", createCF);
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(createCF), false))) {
            writer.write(graph);
        }
    }

    public static String visualize(ControlFlowGraph cfg) {
        StringBuilder graphBuilder = new StringBuilder();

        write(graphBuilder, "digraph G {");
        cfg.getNodes().entrySet().stream()
                .collect(groupingBy(e -> ((NodeAddress) e.getKey()).getInstance()))
                .forEach((instance, entries) -> {
                    if(absolute) {
                        write(graphBuilder, instanceStartAbs);
                    } else {
                        write(graphBuilder, String.format(instanceStart, instance, instance));
                    }
                    // instance -> addresses belonging to instance
                    entries.stream()
                            .collect(groupingBy(e -> ((NodeAddress) e.getKey()).getGraph()))
                            .forEach(
                                    // graph -> addresses belonging to graph and instance
                                    (graph, entries1) -> {
                                        StringBuilder nodes = new StringBuilder();
                                        entries1.forEach(e -> nodes
                                                .append("\"")
                                                .append(e.getKey().getRepresentation())
                                                .append("\" [shape=rectangle, label=\"")
                                                .append(getNodeLabelForAddress(absolute, e))
                                                .append("\"]; "));
                                        if(absolute) {
                                            write(graphBuilder, String.format(subgraphTemplateAbs, nodes.toString()));
                                        } else {
                                            write(graphBuilder, String.format(subgraphTemplate, graph, nodes.toString(), graph));
                                        }
                                    }
                            );
                    if(absolute) {
                        write(graphBuilder, instanceEndAbs);
                    } else {
                        write(graphBuilder, instanceEnd);
                    }
                });

        cfg.getEdges().forEach(controlFlowEdge -> {
                    String style = getStyle(controlFlowEdge);
//                    NodeContainer<? extends Node> addr = job.getNode(controlFlowEdge.getToAddress());
                    write(graphBuilder, "\""+controlFlowEdge.getFromAddress().getRepresentation()+"\" -> "+"\""+controlFlowEdge.getToAddress().getRepresentation()+"\" "+style);
                }
        );

        write(graphBuilder, "}");

        return graphBuilder.toString();
    }

    private static String getNodeLabelForAddress(Boolean abs, Map.Entry<Address, ControlFlowNode> e) {
        NodeAddress adr = (NodeAddress) e.getKey();
        String simpleName = e.getValue().getType();
        if(abs) {
            return
                    (includeInstance ? adr.getInstance()+"\\n" : "") +
                            (includeGraph ? adr.getGraph()+"\\n" : "") +
                            (includeNodeType ? simpleName+"\\n<" : "") +
                            (includeNodeAddress ? "<"+ adr.getNode() +">": "")
                    +""
                    ;
        } else {
            return simpleName+"\\n<"+ adr.getNode()+">";
        }
    }

    private static String getStyle(ControlFlowEdge controlFlowEdge) {
        String style = "[ xlabel=\"" + controlFlowEdge.getDisplayLabel() + "\",";
        if(controlFlowEdge.isDispatched()) {
            style += "style=dashed,";
        }

        if(controlFlowEdge.isMultiple()) {
            style += "color=red";
        }

        return style+"]";
    }

    private static final String subgraphTemplateAbs = "\t\t\t%s\n";
    private static final String subgraphTemplate = "\t\tsubgraph \"cluster_%s\" {\n" +
            "\t\t\tstyle=filled;\n" +
            "\t\t\tcolor=lightgrey;\n" +
            "\t\t\tnode [style=filled,color=white];\n" +
            "\t\t\t%s\n" +
            "\t\t\tlabel = \"%s\";\n" +
            "\t\t}";

    private static final String instanceStartAbs = "\t\t\t\n";
    private static final String instanceStart = "\tsubgraph \"cluster_%s\" {\n" +
            "\t\tstyle=filled;\n" +
            "\t\tcolor=grey;\n" +
            "\t\tlabel = \"%s\";\n";

    private static final String instanceEndAbs = "";
    private static final String instanceEnd = "\t}";

    private static void write(StringBuilder graph, String line) {
        graph.append(line).append("\n");
    }
}
