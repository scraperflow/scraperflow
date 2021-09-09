package scraper.app;

import scraper.annotations.NotNull;
import scraper.annotations.DITarget;
import scraper.api.DIContainer;
import scraper.api.ValidationException;
import scraper.api.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.NodeContainer;
import scraper.api.Node;
import scraper.api.Addon;
import scraper.api.Hook;
import scraper.api.NodeHook;
import scraper.api.ScrapeSpecificationParser;
import scraper.api.ExecutorsService;
import scraper.api.ScrapeInstance;
import scraper.api.ScrapeSpecification;
import scraper.core.JobFactory;
import scraper.utils.StringUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.*;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static scraper.app.CommandPrinter.collectAndPrintCommandLineArguments;
import static scraper.util.DependencyInjectionUtil.getDIContainer;

public class Scraper {

    private @NotNull
    static final System.Logger log = System.getLogger("Scraper");

    // dependencies
    private @NotNull
    final JobFactory jobFactory;
    private @NotNull
    final ExecutorsService executorsService;

    // addons
    private @NotNull
    final Collection<Addon> addons;
    // hooks
    private @NotNull
    final Collection<Hook> hooks;
    // node hooks
    private @NotNull
    final Collection<NodeHook> nodeHooks;
    // spec parsers
    private @NotNull
    final Collection<ScrapeSpecificationParser> parsers;

    // DI container
    private DIContainer pico;

    // specifications and instantiations
    private @NotNull
    final Map<ScrapeSpecification, ScrapeInstance> jobs = new LinkedHashMap<>();

    public Scraper(@NotNull JobFactory jobFactory, @NotNull ExecutorsService executorsService,
                   @NotNull @DITarget(Hook.class) Collection<Hook> hooks,
                   @NotNull @DITarget(Addon.class) Collection<Addon> addons,
                   @NotNull @DITarget(NodeHook.class) Collection<NodeHook> nodeHooks,
                   @NotNull @DITarget(ScrapeSpecificationParser.class) Collection<ScrapeSpecificationParser> parsers
    ) {
        this.jobFactory = jobFactory;
        this.executorsService = executorsService;
        this.hooks = hooks;
        this.addons = addons;
        this.nodeHooks = nodeHooks;
        this.parsers = parsers;
    }

    public static void main(final String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$-1.1s %1$tF %1$tT | %3$s | %5$s %n");

        String helpArgs = StringUtil.getArgument(args, "help");
        if(helpArgs == null) {
            log.log(DEBUG, "To see available command-line arguments, provide the 'help' argument when starting Scraper");
        } else {
            collectAndPrintCommandLineArguments();
            return;
        }

        String versionArgs = StringUtil.getArgument(args, "version");
        if(versionArgs != null) System.out.println("Scraper version 1.0.0-SNAPSHOT");

        // inject dependencies
        DIContainer pico = getDIContainer();
        pico.addComponent(Scraper.class);

        Scraper main = pico.get(Scraper.class);
        requireNonNull(main).pico = pico;

        String nodesArgs = StringUtil.getArgument(args, "nodes");
        if(nodesArgs != null) System.out.println("Nodes: " + main.jobFactory.getNodes());

        try {
            main.run(args);
        }
        catch (ExitException ignored) {}
        catch (Throwable e) {
            log.log(DEBUG, "Could not run scrape job: {0}", e.getMessage());
            if(StringUtil.getArgument(args, "debug-info") == null) surpress(e);
            throw e;
        }
    }

    private static void surpress(Throwable e) {
        e.setStackTrace(new StackTraceElement[0]);
        if(e.getCause() != null) surpress(e.getCause());
    }

    private void run(@NotNull final String... args) throws Exception, ExitException {
        log.log(DEBUG, "Loading {0} addons: {1}", addons.size(), addons);
        for (Addon addon : addons) addon.load(pico, args);

        String userDir = System.getProperty("user.dir");
        log.log(DEBUG, "Loading {0} parsers: {1}", parsers.size(), parsers);
        log.log(DEBUG, "Parsing scrape jobs in {0}", userDir);

        if(parsers.isEmpty()) throw new ValidationException("No Scraper parsers loaded");

        List<ScrapeSpecification> jobDefinitions =
                parsers.stream().map((scrapeSpecificationParser -> scrapeSpecificationParser.parse(pico, args)))
                        .flatMap(List::stream)
                        .peek(spec -> { for (String arg : args) { spec.with(arg); } })
                        .collect(Collectors.toList());


        if(jobDefinitions.isEmpty()) {
            // check for implicit location
            // merge all accepted file endings
            List<ScrapeSpecification> specs = parsers.stream()
                    .map(parser -> parser.filterFiles(
                            ofNullable(new File(userDir).list())
                                    .orElse(new String[0])
                            ).stream()
                                    .map(parser::parseSingle)
                                    .flatMap(Optional::stream)
                                    .peek(spec -> { for (String arg : args) { spec.with(arg); } })
                                    .collect(Collectors.toList())
                    )
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if(specs.size() != 1) {
                log.log(DEBUG, "Too many or no valid implied taskflow specifications found: {0}", specs);
            } else {
                log.log(DEBUG, "Using implied taskflow specification {0}", specs.get(0));
                jobDefinitions.addAll(specs);
            }
        }
        log.log(DEBUG, "Parsed {0} specifications", jobDefinitions.size());

        log.log(DEBUG, "Converting scrape jobs");
        for (ScrapeSpecification jobDefinition : jobDefinitions)
            jobs.put(jobDefinition, jobFactory.convertScrapeJob(args, jobDefinition, nodeHooks));

        StringUtil.getAllArguments(args, "arg")
                .stream()
                .map(JobFactory::parseSingleArgument)
                .forEach(entry
                        -> jobs.forEach((spec, job)
                        -> job.getEntryArguments().put(entry.getKey(), entry.getValue())));


        log.log(DEBUG, "Executing {0} hooks: {1}", hooks.size(), hooks);
        for (Hook hook : hooks) hook.execute(pico, args, jobs);

        if(System.getProperty("scraper.exit", "false").equalsIgnoreCase("true")) {
            log.log(DEBUG, "Exiting Scraper because system property 'scraper.exit' is true");
            throw new ExitException();
        }

        if(jobDefinitions.isEmpty())
            throw new IllegalArgumentException("Too many or no valid taskflow specifications");

        log.log(DEBUG, "Found {0} node hooks: {1}", nodeHooks.size(), nodeHooks);
        startScrapeJobs();
    }

    private void startScrapeJobs() {
        log.log(TRACE, "--------------------------------------------------------");
        log.log(TRACE, "--- Starting Main Threads");
        log.log(TRACE, "--------------------------------------------------------");
        jobs.forEach((definition, job) -> {
            FlowMap initial = FlowMapImpl.origin(job.getEntryArguments());
            Optional<NodeContainer<? extends Node>> entry = job.getEntry();
            if(entry.isEmpty()) {
                log.log(WARNING, "Job has no entry node: {0}", job.getName());
            } else {
                NodeContainer<? extends Node> initialNode = entry.get();
                initialNode.forward(initial, initialNode.getAddress());
            }
        });
    }

}
