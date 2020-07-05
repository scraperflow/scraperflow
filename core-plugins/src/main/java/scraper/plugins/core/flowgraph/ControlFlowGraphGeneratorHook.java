package scraper.plugins.core.flowgraph;


import scraper.annotations.ArgsCommand;
import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;
import scraper.api.plugin.Hook;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.utils.StringUtil;

import java.util.Map;

@ArgsCommand(
        value = "cfg-dot | cfg",
        doc = "Generates a .dot control flow graph out of the currently known scrape jobs.",
        example = "java -jar scraper.jar app.scrape cfg exit"
        )
@ArgsCommand(
        value = "cfg-real",
        doc = "Generates the graph with real control flow",
        example = "java -jar scraper.jar app.scrape cfg cfg-real exit"
)
@ArgsCommand(
        value = "cfg-absolute",
        doc = "Generates the graph without nesting and clustering nodes by instance and graphs",
        example = "java -jar scraper.jar app.scrape cfg cfg-absolute exit"
)
@ArgsCommand(
        value = "cfg-abs-no-instance",
        doc = "If absolute graph is enabled, removes instance labels of each node",
        example = "java -jar scraper.jar app.scrape cfg cfg-absolute cfg-abs-no-instance exit"
)
@ArgsCommand(
        value = "cfg-abs-no-graph",
        doc = "If absolute graph is enabled, removes graph labels of each node",
        example = "java -jar scraper.jar app.scrape cfg cfg-absolute cfg-abs-no-graph exit"
)
@ArgsCommand(
        value = "cfg-abs-no-node-type",
        doc = "If absolute graph is enabled, removes node type labels of each node",
        example = "java -jar scraper.jar app.scrape cfg cfg-absolute cfg-abs-no-node-type exit"
)
@ArgsCommand(
        value = "cfg-abs-no-node-address",
        doc = "If absolute graph is enabled, removes node address labels of each node",
        example = "java -jar scraper.jar app.scrape cfg cfg-absolute cfg-abs-no-node-address exit"
)
public class ControlFlowGraphGeneratorHook implements Hook {

    private static final System.Logger log = System.getLogger("ControlFlowGraphGenerator");

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args, @NotNull Map<ScrapeSpecification, ScrapeInstance> jobs) throws Exception {
        // ==
        // control flow graph generation
        // ==
        String createCF = StringUtil.getArgument(args, "cfg");
        if(createCF == null) createCF = StringUtil.getArgument(args, "cfg-dot");

        boolean realControlFlow = false;

        if (StringUtil.getArgument(args, "cfg-absolute") != null) GraphVisualizer.absolute = true;
        if (StringUtil.getArgument(args, "cfg-abs-no-instance") != null) GraphVisualizer.includeInstance = false;
        if (StringUtil.getArgument(args, "cfg-abs-no-graph") != null) GraphVisualizer.includeGraph = false;
        if (StringUtil.getArgument(args, "cfg-abs-no-node-type") != null) GraphVisualizer.includeNodeType = false;
        if (StringUtil.getArgument(args, "cfg-abs-no-node-address") != null) GraphVisualizer.includeNodeAddress = false;
        if (StringUtil.getArgument(args, "cfg-real") != null) realControlFlow = true;


        if(createCF != null) {
            for (ScrapeSpecification def : jobs.keySet()) {
                ScrapeInstance job = jobs.get(def);
                String path = job.getName().concat(".dot");

                log.log(System.Logger.Level.INFO,"Creating graph for {0}", def.getScrapeFile());
                GraphVisualizer.visualize(job, path, realControlFlow);
            }
        }
    }

    @Override public String toString() { return "ControlFlowGraphGenerator"; }
}
