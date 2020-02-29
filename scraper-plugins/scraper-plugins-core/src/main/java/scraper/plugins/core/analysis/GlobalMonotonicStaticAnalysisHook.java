package scraper.plugins.core.analysis;


import scraper.annotations.ArgsCommand;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.ValidationException;
import scraper.api.plugin.Hook;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.plugins.core.flowgraph.FlowUtil;
import scraper.plugins.core.flowgraph.api.ControlFlowGraph;
import scraper.utils.StringUtil;

import java.util.Map;

@ArgsCommand(
        value = "check",
        doc = "Checks the graph statically with a global and monotonic term to type mapping. Reports if a type error is encountered",
        example = "java -jar scraper.jar app.scrape check exit"
        )
public class GlobalMonotonicStaticAnalysisHook implements Hook {
    @Override
    public void execute(DIContainer dependencies, String[] args, Map<ScrapeSpecification, ScrapeInstance> jobs) throws ValidationException {
        String createCF = StringUtil.getArgument(args, "check");
        if(createCF != null) {
            for (ScrapeSpecification def : jobs.keySet()) {
                ScrapeInstance job = jobs.get(def);
                ControlFlowGraph cfg = FlowUtil.generateControlFlowGraph(job);
                StaticCheckUtil.check(job, cfg);
            }
        }
    }
}
