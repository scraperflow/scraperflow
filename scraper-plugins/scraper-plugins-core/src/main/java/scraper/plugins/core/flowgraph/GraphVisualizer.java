package scraper.plugins.core.flowgraph;

import org.slf4j.Logger;
import scraper.api.node.Address;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
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

// FIXME only works for entry graph
class GraphVisualizer {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GraphVisualizer.class);
    private static StringBuilder graph;

    static Boolean includeInstance = true;
    static Boolean includeGraph = true;
    static Boolean includeNodeType = true;
    static Boolean includeNodeAddress = true;
    static Boolean absolute = false;

    static void visualize(ScrapeInstance job, String createCF) throws FileNotFoundException {
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job);

        log.info("Output: {}", createCF);
        graph = new StringBuilder();

        write("digraph G {");

        cfg.getNodes().entrySet().stream()
                .collect(groupingBy(e -> ((NodeAddress) e.getKey()).getInstance()))
                .forEach((instance, entries) -> {
                    if(absolute) {
                        write(instanceStartAbs);
                    } else {
                        write(String.format(instanceStart, instance, instance));
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
                                                .append(getNodeLabelForAddress(absolute, e, job))
                                                .append("\"]; "));
                                        if(absolute) {
                                            write(String.format(subgraphTemplateAbs, nodes.toString()));
                                        } else {
                                            write(String.format(subgraphTemplate, graph, nodes.toString(), graph));
                                        }
                                    }
                    );
                    if(absolute) {
                        write(instanceEndAbs);
                    } else {
                        write(instanceEnd);
                    }
        });

        cfg.getEdges().forEach(controlFlowEdge -> {
                    String style = getStyle(controlFlowEdge);
                    NodeContainer<? extends Node> addr = job.getNode(controlFlowEdge.getToAddress());
                    write("\""+controlFlowEdge.getFromAddress().getRepresentation()+"\" -> "+"\""+addr.getAddress().getRepresentation()+"\" "+style);
                }
        );

        write("}");

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(createCF), false))) {
            writer.write(graph.toString());
        }

    }

    private static String getNodeLabelForAddress(Boolean abs, Map.Entry<Address, ControlFlowNode> e, ScrapeInstance job) {
        NodeAddress adr = (NodeAddress) e.getKey();
        NodeContainer<? extends Node> node = job.getNode(adr);
        String simpleName = node.getC().getClass().getSimpleName();
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

    private static void write(String line) {
        graph.append(line).append("\n");
    }
}
