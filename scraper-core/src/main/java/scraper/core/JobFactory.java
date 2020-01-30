package scraper.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.plugin.metadata.PluginMetadata;
import org.springframework.plugin.metadata.SimplePluginMetadata;
import scraper.annotations.NotNull;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.InstanceAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.impl.InstanceAddressImpl;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.type.Node;
import scraper.api.node.type.StreamNode;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.util.JobUtil;
import scraper.utils.FileUtil;
import scraper.utils.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static scraper.utils.FileUtil.getFirstExistingPaths;

public class JobFactory {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JobFactory.class);

    public JobFactory(ProxyReservation proxyReservation, HttpService httpService, ExecutorsService executorsService, FileService fileService, PluginBean plugins) {
        this.proxyReservation = proxyReservation;
        this.httpService = httpService;
        this.executorsService = executorsService;
        this.fileService = fileService;
        this.plugins = plugins;
    }

    private final ProxyReservation proxyReservation;
    private final HttpService httpService;
    private final ExecutorsService executorsService;
    private final FileService fileService;

    private final PluginBean plugins;

    private final Map<ScrapeSpecification, Map<String, String>> jobNodeDependencies = new HashMap<>();

    /** Json parser for .scrape files */
    private final ObjectMapper objectMapper = new ObjectMapper();


    private ScrapeInstaceImpl parseJob(ScrapeSpecification def) {
        ScrapeInstaceImpl job = new ScrapeInstaceImpl();
        job.setName(def.getName());
        job.setEntry(def.getEntry());
        job.setGlobalNodeConfigurations(def.getGlobalNodeConfigurations());

        job.setExecutors(executorsService);
        job.setFileService(fileService);
        job.setHttpService(httpService);
        job.setProxyReservation(proxyReservation);

        return job;
    }


    private @NotNull Map<String,String> parseInputArguments(@NotNull final File args) throws IOException {
        final Map<String, String> inputMap =  new HashMap<>();

        StringUtil.readBody(args, line -> {
            if(!line.startsWith("#") && !line.isEmpty()) {
                String key = line.substring(0, line.indexOf("="));
                String value = line.substring(line.indexOf("=")+1);
                inputMap.put(key,value);
            }
        });

        return inputMap;
    }

//    public ScrapeInstaceImpl createEmptyJob() {
//        ScrapeInstaceImpl job = new ScrapeInstaceImpl();
//
//        job.setExecutors(executorsService);
//        job.setFileService(fileService);
//        job.setHttpService(httpService);
//        job.setProxyReservation(proxyReservation);
//
//        return job;
//    }

    public @NotNull ScrapeInstaceImpl convertScrapeJob(@NotNull final ScrapeSpecification jobDefinition) throws IOException, ValidationException {
        return convertJob(jobDefinition, null);
    }

    private @NotNull ScrapeInstaceImpl convertJob(@NotNull final ScrapeSpecification jobDefinition,
                                         Function<String, Map<String, Object>> nodeSupplier) throws IOException, ValidationException {
        // ===
        // Imports
        // ===
        Map<InstanceAddress, ScrapeInstance> parsedImports = new HashMap<>();
        for (String job : jobDefinition.getImports().keySet()) {
//            List<Address> exportedAddresses = jobDefinition.getImports().get(job);
            log.info("Importing '{}' into '{}'", job, jobDefinition.getName());
            List<ScrapeSpecification> importedJob = JobUtil.parseJobs(new String[]{job}, jobDefinition.getPaths().stream().map(Path::toString).collect(Collectors.toSet()));
            if(importedJob.size() > 1) throw new ValidationException("Imported job is ambiguous: " + importedJob.size());
            ScrapeSpecification newJob = importedJob.get(0);

            // overwrite global configuration of imported job
            newJob.getGlobalNodeConfigurations().putAll(jobDefinition.getGlobalNodeConfigurations());

            ScrapeInstaceImpl parsedJob = convertScrapeJob(newJob);
            parsedImports.put(new InstanceAddressImpl(newJob.getName()), parsedJob);
        }

        // ===
        // Node dependencies
        // ===
        if (jobDefinition.getDependencies() != null) {
            try {
                parseNodeDependencies(jobDefinition);
                log.info("Using node dependency file for job '{}': {}",
                        jobDefinition.getScrapeFile(),
                        jobDefinition.getDependencies());
            } catch (FileNotFoundException e) {
                log.warn("Missing node dependency file: '{}'. Continuing without node versioning.",
                        jobDefinition.getDependencies());
            }
        }
//
        // ===
        // Job Pojo
        // ===
        log.info("Parsing {}",jobDefinition.getScrapeFile());
        ScrapeInstaceImpl job = parseJob(jobDefinition);

        // ===
        // .args files
        // ===

        // first args from definition
        Map<String, Object> combinedArgs = new HashMap<>(job.getInitialArguments());

        // implied args
        try {
            String impliedArgs = Paths.get(FileUtil.replaceFileExtension(jobDefinition.getScrapeFile(), "args")).toString();
            File impliedArgsFile = new File(impliedArgs);
            Map<String, String> init = parseInputArguments(impliedArgsFile);
            combinedArgs.putAll(init);
            log.info("Parsed implied args file: {}", impliedArgs);
        } catch (Exception ignored) {}

        // later args files overwrite earlier ones
        for (String arg : jobDefinition.getArguments()) {
            File argsFile = getFirstExistingPaths(arg, jobDefinition.getPaths());
            Map<String, String> init = parseInputArguments(argsFile);
            combinedArgs.putAll(init);
            log.info("Parsed args file: {}", arg);
        }

        job.setInitialArguments(combinedArgs);

        // ===
        // Pre-process fragments
        // ===
        log.info("Pre process fragments");
        for (Address graph : jobDefinition.getGraphs().keySet()) {
            preprocessFragments(jobDefinition.getGraphs().get(graph), jobDefinition);
        }

        // ===
        // Process nodes and instantiate actual matching implementations
        // ===
        for (GraphAddress graphKey : jobDefinition.getGraphs().keySet()) {
            List<Map<String, Object>> graph = jobDefinition.getGraphs().get(graphKey);

            for (Map<String, Object> nodeConfiguration : graph) {
                String nodeType = (String) nodeConfiguration.get("type");

                String vers = jobNodeDependencies
                        .getOrDefault(jobDefinition, new LinkedHashMap<>())
                        .getOrDefault(nodeType, "0.0.0");
                PluginMetadata metadata = new SimplePluginMetadata(nodeType, vers);

                List<? extends AbstractMetadata> processPlugins = plugins.getPlugins().getPluginsFor(metadata);
                Node n = getHighestMatchingPlugin(processPlugins, metadata);

                NodeContainer<? extends Node> nn = getMatchingNodeContainer(n);
                nn.setNodeConfiguration(nodeConfiguration, graphKey);

                job.getGraphs().putIfAbsent(graphKey, new ArrayList<>());
                job.getGraph(graphKey).add(nn);
            }
        }

        job.getImportedInstances().putAll(parsedImports);
        for (InstanceAddress address : parsedImports.keySet()) {
            parsedImports.get(address).init();
        }

        job.init();
        return job;
    }

    private NodeContainer<? extends Node> getMatchingNodeContainer(Node n) {
        if(n instanceof FunctionalNode) {
            return new AbstractFunctionalNode() { @Override public FunctionalNode getC() { return (FunctionalNode) n; } };
        }
        else if(n instanceof StreamNode) {
            return new AbstractStreamNode() { @Override public StreamNode getC() { return (StreamNode) n; } };
        } else {
            // default, generic node
            return new GenericNode() { @Override public Node getC() { return n; } };
        }
    }


    private void parseNodeDependencies(@NotNull final ScrapeSpecification jobDefinition) throws IOException {
        if(jobDefinition.getDependencies() != null) {
            File ndep = getFirstExistingPaths(jobDefinition.getDependencies(), jobDefinition.getPaths());

            Map<String, String> nodeDependencies = jobNodeDependencies.getOrDefault(jobDefinition, new LinkedHashMap<>());
            StringUtil.readBody(ndep, line -> {
                // TODO validate
                String node = line.split(":")[0];
                String version = line.split(":")[1];

                if (nodeDependencies.get(node) != null) log.warn("Node dependency already contained, overwriting: {}", node);
                nodeDependencies.put(node, version);
            });
            jobNodeDependencies.put(jobDefinition, nodeDependencies);
        }

    }



    private void preprocessFragments(List<Map<String, Object>> graph, ScrapeSpecification jobDefinition) throws IOException {
        boolean fragmentFound;
        do {
            fragmentFound = false;

            Iterator<Map<String, Object>> iter = graph.iterator();
            int i = 0;

            // iterate over all current process nodes in the definition
            while (iter.hasNext()) {
                Map<String, Object> nodeConfig = iter.next();

                // check if node is a fragment
                if(((String) nodeConfig.get("type")).equalsIgnoreCase("fragment")) {

                    // find fragment file
                    String location = (String) nodeConfig.get("required");
                    File fragment = getFirstExistingPaths(location, jobDefinition.getPaths());

                    List<Map<String, Object>> replaced = generateFragmentRecursive(fragment, jobDefinition, location);

                    iter.remove();

                    for (int i1 = 0; i1 < replaced.size(); i1++) {
                        graph.add(i+i1, replaced.get(i1));
                    }

                    fragmentFound = true;
                    break;
                }

                i++;
            }
        } while (fragmentFound);
    }


    private List<Map<String, Object>> generateFragmentRecursive(
            File fragmentPath,
            ScrapeSpecification jobDefinition,
            String fragmentName
    ) throws IOException {
        // read fragment content
        List<FragmentDefinition> fragmentDefinitions = objectMapper.readValue(fragmentPath, FragmentDefinitionList.class);

        List<Map<String, Object>> fragments = new LinkedList<>();

        // go through fragment node list
        for (Map<String, Object> node : fragmentDefinitions) {
            if(((String) node.get("type")).equalsIgnoreCase("fragment")) {
                String location = (String) node.get("required");
                // fragment inside fragment found, get path
                File fragment = getFirstExistingPaths(location, jobDefinition.getPaths());

                // generate fragments
                List<Map<String, Object>> generated = generateFragmentRecursive(fragment, jobDefinition, location);

                // add all in order
                fragments.addAll(generated);
            } else {
                // not a fragment
                node.put("fragment", fragmentName);
                fragments.add(node);
            }
        }

        return fragments;
    }

    public List<? extends AbstractMetadata> getPlugins() {
        return plugins.getPlugins().getPlugins();
    }

    // export currently not used
//    public void writePojo(String path, ScrapeInstance job) throws IOException {
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
//        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), job);
//    }

    private static class FragmentDefinitionList extends ArrayList<FragmentDefinition> {}
    private static class FragmentDefinition extends LinkedHashMap<String, Object> {}

    @NotNull
    private Node getHighestMatchingPlugin(@NotNull final List<? extends AbstractMetadata> processPlugins,
                                          @NotNull final PluginMetadata metadata) throws ValidationException {
        AbstractMetadata curr = null;

        for (AbstractMetadata processPlugin : processPlugins) {
            if(curr == null) {
                curr = processPlugin;
            } else {
                if(processPlugin.backwardsCompatible(curr)) curr = processPlugin;
            }
        }

        if(curr == null) {
            String msg = "No plugin for "+metadata.getName()+" (v"+metadata.getVersion()+") found! " +
                    "Provide an implementation with qualifying version number.";
            throw new ValidationException(msg);
        }
        return curr.getNode();
    }
}
