package scraper.plugins.core.typechecker;


import org.slf4j.Logger;
import scraper.annotations.ArgsCommand;
import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;
import scraper.api.plugin.Hook;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.utils.StringUtil;

import java.util.Map;

@ArgsCommand(
        value = "disable-typing",
        doc = "Disables static type checking",
        example = "java -jar scraper.jar app.scrape disable-typing"
        )
public class TypeHook implements Hook {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TypeHook.class);

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args, @NotNull Map<ScrapeSpecification, ScrapeInstance> jobs) {
        // ==
        // control flow graph generation
        // ==
        String disable = StringUtil.getArgument(args, "disable-typing");

        if(disable == null) {
            for (ScrapeSpecification def : jobs.keySet()) {
                ScrapeInstance job = jobs.get(def);

                log.info("Static type check for {}", def.getScrapeFile());
                ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job, false);
                TypeChecker t = new TypeChecker();
                t.typeTaskflow(job, cfg);
            }
        }
    }

    @Override public String toString() { return "ControlFlowGraphGenerator"; }
}
