package scraper.app;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import scraper.annotations.ArgsCommand;
import scraper.annotations.ArgsCommands;
import scraper.annotations.di.DITarget;
import scraper.api.di.DIContainer;
import scraper.api.exceptions.NodeException;
import scraper.api.flow.FlowMap;
import scraper.api.plugin.Addon;
import scraper.api.plugin.Hook;
import scraper.api.plugin.PreHook;
import scraper.api.service.CandidatePathService;
import scraper.api.service.ExecutorsService;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.utils.StringUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static scraper.util.DependencyInjectionUtil.getDIContainer;
import static scraper.util.JobUtil.parseJobs;
import static scraper.util.NodeUtil.flowOf;

public class Scraper {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Scraper.class);

    // dependencies
    private final JobFactory jobFactory;
    private final ExecutorsService executorsService;
    private final CandidatePathService pathService;


    // addons
    private final Collection<Addon> addons;
    // pre parse job hooks
    private final Collection<PreHook> prehooks;
    // hooks
    private final Collection<Hook> hooks;

    // DI container
    public DIContainer pico;

    // saved jobs and dispatched futures
    private final Map<ScrapeSpecification, ScrapeInstance> jobs = new LinkedHashMap<>();

    public Scraper(JobFactory jobFactory, ExecutorsService executorsService, CandidatePathService pathService,
                   @DITarget(PreHook.class) Collection<PreHook> prehooks,
                   @DITarget(Hook.class) Collection<Hook> hooks,
                   @DITarget(Addon.class) Collection<Addon> addons) {
        this.jobFactory = jobFactory;
        this.executorsService = executorsService;
        this.pathService = pathService;
        this.prehooks = prehooks;
        this.hooks = hooks;
        this.addons = addons;
    }


    public static void main(String[] args) throws Exception {
        printBanner();

        String helpArgs = StringUtil.getArgument(args, "help");
        if(helpArgs == null) log.info("To see available command-line arguments, provide the 'help' argument when starting Scraper");
        else {
            collectAndPrintCommandLineArguments();
            return;
        }

        // inject dependencies
        DIContainer pico = getDIContainer();
        pico.addComponent(Scraper.class);

        Scraper main = pico.get(Scraper.class);
        main.pico = pico;

        main.run(args);
    }


    public void run(String... args) throws Exception {
        log.info("Loading {} hooks", addons.size());
        for (Addon addon : addons) addon.load(pico);

        log.info("Executing {} pre-hooks", prehooks.size());
        for (PreHook hook : prehooks)
            hook.execute(pico, args);

        log.info("Parsing scrape jobs");
        List<ScrapeSpecification> jobDefinitions = parseJobs(args, pathService.getCandidatePaths());

        log.info("Converting scrape jobs");
        for (ScrapeSpecification jobDefinition : jobDefinitions) {
            jobs.put(jobDefinition, jobFactory.convertScrapeJob(jobDefinition));
        }

        log.info("Executing {} hooks", hooks.size());
        for (Hook hook : hooks) hook.execute(pico, args, jobs);

        if(System.getProperty("scraper.exit", "false").equalsIgnoreCase("true")) {
            log.info("Exiting Scraper because system property 'scraper.exit' is true");
            return;
        }

        log.info("Starting scrape jobs");
        startScrapeJobs();
    }

    private void startScrapeJobs() {
    	log.info("--------------------------------------------------------");
    	log.info("--- Starting Main Threads");
		log.info("--------------------------------------------------------");
		jobs.forEach((definition, job) -> {
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                try {
                    FlowMap initial = flowOf(job.getInitialArguments());

                    job.getProcessNode(String.valueOf(0)).accept(initial);

                } catch (NodeException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }, executorsService.getService("main"));

            future.exceptionally(e -> {
                log.error("'{}' failed: {}", job.getName(), e);
                return null;
            });
		});
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
                AnnotationInfo argsCommands = (routeClassInfo.getAnnotationInfo(ArgsCommands.class.getName()));
                for (Object value : ((Object[]) argsCommands.getParameterValues().get("value"))) {
                    printArgsCommand(((AnnotationInfo) value));
                }
            }
        }
    }

    private static void printArgsCommand(AnnotationInfo argsCommand) {
        String val = (String) argsCommand.getParameterValues().get("value");
        String doc = (String) argsCommand.getParameterValues().get("doc");
        String example = (String) argsCommand.getParameterValues().get("example");


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
