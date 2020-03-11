package scraper.app;

import io.github.classgraph.*;
import org.slf4j.Logger;
import scraper.annotations.ArgsCommand;
import scraper.annotations.ArgsCommands;
import scraper.annotations.NotNull;
import scraper.annotations.di.DITarget;
import scraper.api.di.DIContainer;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.plugin.Addon;
import scraper.api.plugin.Hook;
import scraper.api.plugin.NodeHook;
import scraper.api.service.ExecutorsService;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.utils.StringUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;
import static scraper.util.DependencyInjectionUtil.getDIContainer;
import static scraper.util.JobUtil.parseJobs;

@ArgsCommand(
        value = "debug-info",
        doc = "Returns more information about unexpected errors",
        example = "scraper debug-info app.yt"
)
public class Scraper {

    private @NotNull static final Logger log = org.slf4j.LoggerFactory.getLogger("Scraper");

    // dependencies
    private @NotNull final JobFactory jobFactory;
    private @NotNull final ExecutorsService executorsService;

    // addons
    private @NotNull final Collection<Addon> addons;
    // hooks
    private @NotNull final Collection<Hook> hooks;
    // node hooks
    private @NotNull final Collection<NodeHook> nodeHooks;

    // DI container
    private DIContainer pico;

    // specifications and instantiations
    private @NotNull final Map<ScrapeSpecification, ScrapeInstance> jobs = new LinkedHashMap<>();

    public Scraper(@NotNull JobFactory jobFactory, @NotNull ExecutorsService executorsService,
                   @NotNull @DITarget(Hook.class) Collection<Hook> hooks,
                   @NotNull @DITarget(Addon.class) Collection<Addon> addons,
                   @NotNull @DITarget(NodeHook.class) Collection<NodeHook> nodeHooks) {
        this.jobFactory = jobFactory;
        this.executorsService = executorsService;
        this.hooks = hooks;
        this.addons = addons;
        this.nodeHooks = nodeHooks;
    }


    public static void main(@NotNull final String[] args) throws Exception {
        printBanner();

        String helpArgs = StringUtil.getArgument(args, "help");
        if(helpArgs == null) {
            log.info("To see available command-line arguments, provide the 'help' argument when starting Scraper");
        } else {
            collectAndPrintCommandLineArguments();
            return;
        }

        // inject dependencies
        DIContainer pico = getDIContainer();
        pico.addComponent(Scraper.class);

        Scraper main = pico.get(Scraper.class);
        requireNonNull(main).pico = pico;

        String exitWithException = System.getProperty("exitWithException", "false");
        if(exitWithException.equalsIgnoreCase("false")) {
            try { main.run(args); } catch (Exception e) {
                log.error("Could not run scrape job: {}", e.getMessage());
                if(StringUtil.getArgument(args, "debug-info") != null) e.printStackTrace();
            }
        } else {
            main.run(args);
        }
    }


    private void run(@NotNull final String... args) throws Exception {
        log.info("Loading {} addons: {}", addons.size(), addons);
//        log.info("Discovered {} nodes, {}", plugins.getPlugins().size(), sb.append("]").toString());
        for (Addon addon : addons) addon.load(pico, args);

        log.info("Parsing scrape jobs in {}", System.getProperty("user.dir"));
        List<ScrapeSpecification> jobDefinitions = parseJobs(args);

        log.debug("Converting scrape jobs");
        for (ScrapeSpecification jobDefinition : jobDefinitions)
            jobs.put(jobDefinition, jobFactory.convertScrapeJob(jobDefinition, nodeHooks));

        log.info("Executing {} hooks: {}", hooks.size(), hooks);
        for (Hook hook : hooks) hook.execute(pico, args, jobs);

        if(System.getProperty("scraper.exit", "false").equalsIgnoreCase("true")) {
            log.info("Exiting Scraper because system property 'scraper.exit' is true");
            return;
        }

        log.info("Found {} node hooks: {}", nodeHooks.size(), nodeHooks);
        startScrapeJobs();
    }

    private void startScrapeJobs() {
        log.info("--------------------------------------------------------");
        log.info("--- Starting Main Threads");
        log.info("--------------------------------------------------------");
        List<CompletableFuture<FlowMap>>  futures = new ArrayList<>();
        jobs.forEach((definition, job) -> {
            CompletableFuture<FlowMap> future = CompletableFuture.supplyAsync(() -> {
                try {
                    FlowMap initial = FlowMapImpl.origin(job.getEntryArguments());
                    Optional<NodeContainer<? extends Node>> entry = job.getEntry();
                    if(entry.isEmpty()) {
                        log.warn("Job has no entry node: {}", job.getName());
                    } else {
                        NodeContainer<? extends Node> initialNode = entry.get();
                        initialNode.getC().accept(initialNode, initial);
                    }

                    return initial;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executorsService.getService(definition.getName(), "main", 1));

            future.exceptionally(e -> {
                log.error("'{}' failed: {}", job.getName(), e.getMessage());
                return null;
            });

            futures.add(future);
        });

        futures.forEach(CompletableFuture::join);
    }


    private static void printBanner() {
        try (InputStream is = Scraper.class.getResourceAsStream("/banner.txt")) {
            new BufferedReader(new InputStreamReader(is)).lines().forEachOrdered(System.out::println);
        } catch (Exception e) {
            log.warn("Could not print banner", e);
        }
    }

    private static void collectAndPrintCommandLineArguments() {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo()
                .whitelistPackages("scraper")
                .scan()) {
            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(ArgsCommand.class.getName())) {
                printArgsCommand(routeClassInfo.getAnnotationInfo(ArgsCommand.class.getName()));
            }

            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(ArgsCommands.class.getName())) {
                AnnotationInfoList argsCommands = (routeClassInfo.getAnnotationInfoRepeatable(ArgsCommand.class.getName()));
                for (AnnotationInfo argsCommand : argsCommands) {
                    printArgsCommand(argsCommand);
                }
            }
        }
    }

    private static void printArgsCommand(@NotNull final AnnotationInfo argsCommand) {
        String val = (String) argsCommand.getParameterValues().getValue("value");
        String doc = (String) argsCommand.getParameterValues().getValue("doc");
        String example = (String) argsCommand.getParameterValues().getValue("example");


        System.out.println("-------------------");
        System.out.println(val);
        System.out.println();
        System.out.println("  "+ doc);
        System.out.println();
        System.out.println(" example usage");
        System.out.println();
        System.out.println("  "+ example);
        System.out.println();
        System.out.println();
    }
}
