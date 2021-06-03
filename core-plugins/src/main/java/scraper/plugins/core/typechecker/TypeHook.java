package scraper.plugins.core.typechecker;


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
        value = "disable-typing",
        doc = "Disables static type checking",
        example = "java -jar scraper.jar app.scrape disable-typing"
        )
public class TypeHook implements Hook {

    private static final System.Logger log = System.getLogger("TypeChecker");

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args, @NotNull Map<ScrapeSpecification, ScrapeInstance> jobs) {
        // ==
        // control flow graph generation
        // ==
        String disable = StringUtil.getArgument(args, "disable-typing");

        if(disable == null) {
            for (ScrapeSpecification def : jobs.keySet()) {
                ScrapeInstance job = jobs.get(def);

                log.log(System.Logger.Level.DEBUG,"Static type check for {0}", def.getScrapeFile());
                ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job);


                TypeChecker t = new TypeChecker();
                t.typeTaskflow(job, cfg);

                // first typing without cycle information
                t.typeTaskflow(job, cfg);

                // second typing with cycle information
                t.typeTaskflow(job, cfg);
            }
        }
    }

    @Override public String toString() { return "StaticTypeCheck"; }
}
