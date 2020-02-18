package scraper.plugins.core.flowgraph;

import org.slf4j.Logger;
import scraper.api.node.NodeAddress;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.plugins.core.flowgraph.api.ControlFlowEdge;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.util.NodeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

// FIXME only works for entry graph
class GraphVisualizer {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GraphVisualizer.class);
    private static StringBuilder graph;

    static void visualize(ScrapeInstance job, String createCF) throws FileNotFoundException {
        ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job);

        log.info("Output: {}", createCF);




        graph = new StringBuilder();

        write("digraph G {");

//        cfg.getNodes().forEach((a,n) ->
//                write("  " + ((NodeAddress) a).getNode() + " [label=\""+a.toString()+"\"]")
//        );

        cfg.getNodes().entrySet().stream()
                .collect(groupingBy(e -> ((NodeAddress) e.getKey()).getInstance()))
                .forEach((instance, entries) -> {
                    write(String.format(instanceStart, instance, instance));
                    // instance -> addresses belonging to instance
                    entries.stream()
                            .collect(groupingBy(e -> ((NodeAddress) e.getKey()).getGraph()))
                            .forEach(
                                    // graph -> addresses belonging to graph and instance
                                    (graph, entries1) -> {
                                        StringBuilder nodes = new StringBuilder();
                                        entries1.forEach(e -> nodes.append("\"").append(e.getKey().getRepresentation()).append("\"; "));
                                        write(String.format(subgraphTemplate, graph, nodes.toString(), graph));
                                    }
                    );
                    write(instanceEnd);
        });

        cfg.getEdges().forEach(controlFlowEdge ->
                write("\""+controlFlowEdge.getFromAddress().getRepresentation()+"\" -> "+"\""+job.getNode(controlFlowEdge.getToAddress()).get().getAddress().getRepresentation()+"\"")
        );

        // define label mappings

//        for (Node node : mapping.keySet()) {
//            Integer id = mapping.get(node);
//            String statefulStr = "";
//            if(node instanceof AbstractFunctionalNode) statefulStr += ", color=\"green\"";
//            write("  " + id + " [label=\""+node.getDisplayName()+"\""+ statefulStr+"]");
//        }
//
//        printSubgraphFragments(job, mapping);
//
//        for (Node node : job.getEntryGraph()) {
//            Integer current = mapping.get(node);
//
//            for (ControlFlowEdge s : node.getOutput()) {
//                // FIXME
////                Integer target = mapping.get(job.getNode(NodeUtil.addressOf(s.getTargetLabel())));
////                write("    "+current +" -> "+target+" [label=\""+s.getLabel()+"\""
////                        +(s.isMultiple() ? ",color=red" : "")
////                        +(s.isDispatched() ? ",style=dotted" : "")
////                        +", arrowhead=vee]");
//            }
//
//            if(node.getInput().isEmpty() && node.getOutput().isEmpty()) {
//                write("    \""+current +"\" [color=red, style=filled, fillcolor=red]");
//            }
//        }
        write("}");

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(new File(createCF), false))) {
            writer.write(graph.toString());
        }

    }

//    private static void printSubgraphFragments(ScrapeInstance job, Map<Node, Integer> mapping) {
//        Map<String, List<Node>> fragments = new HashMap<>();
//        for (Node node : job.getEntryGraph()) {
//            if(node.getFragment() == null) continue;
//
//            List<Node> arr = fragments.getOrDefault(node.getFragment(), new ArrayList<>());
//            arr.add(node);
//            fragments.put(node.getFragment(), arr);
//        }
//
//        int frag = 1;
//        for (String fragment : fragments.keySet()) {
//            List<Node> nodes = fragments.get(fragment);
//            StringBuilder nodesString = new StringBuilder();
//
//            for (Node node : nodes) {
//                nodesString.append(mapping.get(node)).append(";");
//            }
//
//            write(String.format(subgraphTemplate, frag, nodesString.toString(), fragment));
//
//            frag++;
//        }
//
//    }

    private static final String subgraphTemplate = "\t\tsubgraph cluster_%s {\n" +
            "\t\t\tstyle=filled;\n" +
            "\t\t\tcolor=lightgrey;\n" +
            "\t\t\tnode [style=filled,color=white];\n" +
            "\t\t\t%s\n" +
            "\t\t\tlabel = \"%s\";\n" +
            "\t\t}";

    private static final String instanceStart = "\tsubgraph cluster_%s {\n" +
            "\t\tstyle=filled;\n" +
            "\t\tcolor=brown;\n" +
            "\t\tlabel = \"%s\";\n";

    private static final String instanceEnd = "\t}";

    private static void write(String line) {
        graph.append(line).append("\n");
    }
}
