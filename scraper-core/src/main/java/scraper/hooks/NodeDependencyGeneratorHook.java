package scraper.hooks;

import org.slf4j.Logger;
import scraper.annotations.ArgsCommand;
import scraper.annotations.NotNull;
import scraper.annotations.node.NodePlugin;
import scraper.api.di.DIContainer;
import scraper.api.plugin.Hook;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.utils.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Hook to generate a node dependency file for the currently parsed workflow specifications
 */
@ArgsCommand(
        value = "ndep",
        doc = "Generated a node dependency file for all parsed scrape specifications",
        example = "scraper app.scrape ndep exit"
)
public class NodeDependencyGeneratorHook implements Hook {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger("NodeVersioning");

    @Override
    public void execute(@NotNull final DIContainer dependencies, @NotNull final String[] args,
                        @NotNull final Map<ScrapeSpecification, ScrapeInstance> jobs) throws Exception {
        // ==
        // node dependency generation
        // ==
        String createNodeDependencies = StringUtil.getArgument(args, "ndep");
        if(createNodeDependencies != null) {
            log.info("Generating node dependencies");

            for (ScrapeSpecification job : jobs.keySet()) {
                Path path;
                if(createNodeDependencies.isEmpty())
                    path = Path.of(job.getScrapeFile().toString()+".ndep");
                else
                    path = Path.of(createNodeDependencies, job.getName()+".ndep");

                log.info("Creating node dependencies file at {}", path.toString());
                generateNodeDependencies(jobs.get(job), path.toString());
            }
        }
    }

    private void generateNodeDependencies(ScrapeInstance scrapeInstance, String path) throws FileNotFoundException {
        File f = new File(path);
        Set<String> nodes = new HashSet<>();

        scrapeInstance.getRoutes().forEach(((address, nodeContainer) -> {
            String type = nodeContainer.getC().getClass().getSimpleName();
            String version = nodeContainer.getC().getClass().getDeclaredAnnotation(NodePlugin.class).value();
            nodes.add(type+":"+version);
        }));

        try(PrintWriter pw = new PrintWriter(new FileOutputStream(f))) {
            for (String node : nodes) {
                pw.println(node);
            }
        }
    }

    @Override public String toString() { return "NodeDependencyGenerator"; }
}
