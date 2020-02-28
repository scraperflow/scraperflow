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
        value = "cfg-dot",
        doc = "Generates a .dot control flow graph out of the currently known scrape jobs.",
        example = "java -jar scraper.jar app.scrape cfg exit"
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

//    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ControlFlowGraphGeneratorHook.class);

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args, @NotNull Map<ScrapeSpecification, ScrapeInstance> jobs) throws Exception {
        // ==
        // control flow graph generation
        // ==
        String createCF = StringUtil.getArgument(args, "cfg");
        if (StringUtil.getArgument(args, "cfg-absolute") != null) GraphVisualizer.absolute = true;
        if (StringUtil.getArgument(args, "cfg-abs-no-instance") != null) GraphVisualizer.includeInstance = false;
        if (StringUtil.getArgument(args, "cfg-abs-no-graph") != null) GraphVisualizer.includeGraph = false;
        if (StringUtil.getArgument(args, "cfg-abs-no-node-type") != null) GraphVisualizer.includeNodeType = false;
        if (StringUtil.getArgument(args, "cfg-abs-no-node-address") != null) GraphVisualizer.includeNodeAddress = false;

        if(createCF != null) {
            for (ScrapeSpecification def : jobs.keySet()) {
                ScrapeInstance job = jobs.get(def);
                String path = job.getName().concat(".dot");

                System.out.println("Creating graph for {}" + def.getScrapeFile());
                GraphVisualizer.visualize(job, path);
            }
        }
    }
}
