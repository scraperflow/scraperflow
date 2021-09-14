package scraper.plugins.core.pointerfree;


import scraper.annotations.ArgsCommand;
import scraper.annotations.NotNull;
import scraper.api.DIContainer;
import scraper.api.Hook;
import scraper.api.ScrapeInstance;
import scraper.api.ScrapeSpecification;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.utils.StringUtil;

import java.util.Map;

@ArgsCommand(
        value = "disable-pointerfree",
        doc = "Disables pointer-free flow",
        example = "java -jar scraper.jar app.scrape disable-pointerfree"
        )
public class PointerFreeHook implements Hook {

    private static final System.Logger log = System.getLogger("PointerFree");

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args, @NotNull Map<ScrapeSpecification, ScrapeInstance> jobs) {
        // ==
        // control flow graph generation
        // ==
        String disable = StringUtil.getArgument(args, "disable-pointerfree");

        if(disable == null) {
            for (ScrapeSpecification def : jobs.keySet()) {
                ScrapeInstance job = jobs.get(def);

                log.log(System.Logger.Level.DEBUG,"Mutating pointer-free flow for {0}", def.getScrapeFile());
                ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job);

                PointerFreeMutator t = new PointerFreeMutator();
                t.mutateTaskFlow(job, def, cfg);
            }
        }
    }

    @Override public String toString() { return "PointerFree"; }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public boolean preValidation() {
        return true;
    }
}
