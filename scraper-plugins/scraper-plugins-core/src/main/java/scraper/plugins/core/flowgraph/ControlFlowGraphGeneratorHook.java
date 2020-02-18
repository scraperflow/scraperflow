package scraper.plugins.core.flowgraph;


import scraper.annotations.ArgsCommand;
import scraper.api.di.DIContainer;
import scraper.api.plugin.Hook;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.utils.StringUtil;

import java.util.Map;

@ArgsCommand(
        value = "cf",
        doc = "Generates a .dot control flow graph out of the currently known scrape jobs.",
        example = "java -jar scraper.jar app.scrape cf exit"
        )
public class ControlFlowGraphGeneratorHook implements Hook {

//    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ControlFlowGraphGeneratorHook.class);

    @Override
    public void execute(DIContainer dependencies, String[] args, Map<ScrapeSpecification, ScrapeInstance> jobs) throws Exception {
        // ==
        // control flow graph generation
        // ==
        String createCF = StringUtil.getArgument(args, "cf");

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
