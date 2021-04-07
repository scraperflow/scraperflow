package scraper.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import scraper.annotations.NotNull;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.InstanceAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.impl.GraphAddressImpl;
import scraper.api.node.impl.InstanceAddressImpl;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.type.Node;
import scraper.api.node.type.StreamNode;
import scraper.api.plugin.NodeHook;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.utils.FileUtil;
import scraper.utils.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.*;
import static scraper.utils.FileUtil.getFirstExistingPaths;

public class JobFactory {
    private static final System.Logger log = System.getLogger("JobFactory");

    public JobFactory(@NotNull ProxyReservation proxyReservation, @NotNull HttpService httpService,
                      @NotNull ExecutorsService executorsService, @NotNull FileService fileService,
                      @NotNull PluginBean plugins) {
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
    private static final ObjectMapper objectMapper = new ObjectMapper();


    private ScrapeInstaceImpl parseJob(ScrapeSpecification def, Collection<NodeHook> hooks) {
        ScrapeInstaceImpl job = new ScrapeInstaceImpl(def);
        job.setName(def.getName());

        job.setExecutors(executorsService);
        job.setFileService(fileService);
        job.setHttpService(httpService);
        job.setProxyReservation(proxyReservation);

        hooks.forEach(job::addHook);

        return job;
    }

    public static Map.Entry<String, Object> parseSingleArgument (String line) {
        String key = line.substring(0, line.indexOf("="));
        String value = line.substring(line.indexOf("=")+1);
        Object valueObject;
        try {
            valueObject = objectMapper.readValue(value, Object.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse argument for key "+ key+": "+e.getMessage());
        }
        return new AbstractMap.SimpleEntry<>(key, valueObject);
    }

    private @NotNull Map<String, Object> parseInputArguments(@NotNull final File args) throws IOException {
        final Map<String, Object> inputMap =  new HashMap<>();

        StringUtil.readBody(args, line -> {
            if(!line.startsWith("#") && !line.isEmpty()) {
                Map.Entry<String, Object> arg = parseSingleArgument(line);
                inputMap.put(arg.getKey(), arg.getValue());
            }
        });

        return inputMap;
    }

    public @NotNull ScrapeInstaceImpl convertScrapeJob(@NotNull final ScrapeSpecification jobDefinition) throws IOException, ValidationException {
        return convertJob(jobDefinition, null, Set.of());
    }

    public @NotNull ScrapeInstaceImpl convertScrapeJob(@NotNull final ScrapeSpecification jobDefinition, Collection<NodeHook> nodeHooks) throws IOException, ValidationException {
        return convertJob(jobDefinition, null, nodeHooks);
    }

    private @NotNull ScrapeInstaceImpl convertJob(@NotNull final ScrapeSpecification jobDefinition,
                                         Function<String, Map<String, Object>> nodeSupplier,
                                                  Collection<NodeHook> nodeHooks)
            throws IOException, ValidationException {

        // ===
        // Imports
        // ===
        Map<InstanceAddress, ScrapeInstance> parsedImports = new HashMap<>();
        for (String job : jobDefinition.getImports().keySet()) {
            log.log(DEBUG, "Importing {0} into {1}", job, jobDefinition.getName());
            ScrapeSpecification newJob = jobDefinition.getImports().get(job).getSpec();

            // overwrite global configuration of imported job
            newJob.getGlobalNodeConfigurations().putAll(jobDefinition.getGlobalNodeConfigurations());

            ScrapeInstaceImpl parsedJob = convertScrapeJob(newJob, nodeHooks);
            parsedImports.put(new InstanceAddressImpl(newJob.getName()), parsedJob);
        }

        // ===
        // Node dependencies
        // ===
        if (jobDefinition.getDependencies().isPresent()) {
            try {
                parseNodeDependencies(jobDefinition);
                log.log(DEBUG, "Using node dependency file for job {0}: {1}",
                        jobDefinition.getScrapeFile(),
                        jobDefinition.getDependencies());
            } catch (FileNotFoundException e) {
                log.log(DEBUG,  "Missing node dependency file: {0}. Continuing without node versioning.",
                        jobDefinition.getDependencies());
            }
        }
//
        // ===
        // Job Pojo
        // ===
        log.log(DEBUG, "Parsing {0}",jobDefinition.getScrapeFile());
        ScrapeInstaceImpl job = parseJob(jobDefinition, nodeHooks);

        // ===
        // .args files
        // ===

        // first args from definition
        Map<String, Object> combinedArgs = new HashMap<>(job.getEntryArguments());

        // implied args
        try {
            String impliedArgs = Paths.get(FileUtil.replaceFileExtension(jobDefinition.getScrapeFile(), "args")).toString();
            File impliedArgsFile = new File(impliedArgs);
            Map<String, Object> init = parseInputArguments(impliedArgsFile);
            combinedArgs.putAll(init);
            log.log(DEBUG, "Parsed implied args file: {0}", impliedArgs);
        } catch (Exception ignored) {}

        // later args files overwrite earlier ones
        for (String arg : jobDefinition.getArguments()) {
            File argsFile = getFirstExistingPaths(arg, jobDefinition.getPaths());
            Map<String, Object> init = parseInputArguments(argsFile);
            combinedArgs.putAll(init);
            log.log(DEBUG, "Parsed args file: {0}", arg);
        }

        job.getEntryArguments().putAll(combinedArgs);

        // ===
        // Pre-process fragments
        // ===
        log.log(DEBUG, "Pre process fragments");
        for (String graph : jobDefinition.getGraphs().keySet()) {
            preprocessFragments(jobDefinition.getGraphs().get(graph), jobDefinition);
        }


        // ===
        // Process nodes and instantiate actual matching implementations
        // ===
        for (String graphKey : jobDefinition.getGraphs().keySet()) {
            List<Map<String, Object>> graph = jobDefinition.getGraphs().get(graphKey);
            int i = 0;

            for (Map<String, Object> nodeConfiguration : graph) {
                String nodeType = (String) nodeConfiguration.get("type");
                String nodeAddress = (String) nodeConfiguration.get("label");

                String vers = jobNodeDependencies
                        .getOrDefault(jobDefinition, new LinkedHashMap<>())
                        .getOrDefault(nodeType, "0.0.0");

                List<AbstractMetadata> processPlugins = plugins
                        .getPlugins()
                        .stream()
                        .filter(p -> p.supports(nodeType, vers))
                        .collect(Collectors.toList());

                Node n = getHighestMatchingPlugin(processPlugins, nodeType, vers);

                NodeContainer<? extends Node> nn = getMatchingNodeContainer(
                        job.getName(), graphKey, nodeAddress, i, n
                );
                nn.setNodeConfiguration(nodeConfiguration, job.getName(), graphKey);


                GraphAddressImpl graphAddress = new GraphAddressImpl(jobDefinition.getName(), graphKey);

                // instance address if entry and first node
                if(job.getSpecification().getEntry().equalsIgnoreCase(graphKey) && i == 0)
                   job.addRoute(new InstanceAddressImpl(job.getName()), nn);

                // graph address if first node
                if(i == 0) job.addRoute(graphAddress, nn);

                // absolute address
                job.addRoute(nn.getAddress(), nn);

                // set entry
                if(i == 0 && graphKey.equalsIgnoreCase(jobDefinition.getEntry())) job.setEntry(graphAddress, nn);

                i++;
            }
        }

        job.importedInstances.putAll(parsedImports);

        List<Map.Entry<InstanceAddress, ScrapeInstance>> nested = job.importedInstances.entrySet()
                .stream()
                .flatMap(e -> e.getValue().getImportedInstances().entrySet().stream())
                .collect(Collectors.toList());

        nested.forEach(e -> job.importedInstances.put(e.getKey(), e.getValue()));

        job.init();

        job.importedInstances.forEach((a,i)-> i.getRoutes().forEach(job::addRoute));

        job.validate();
        return job;
    }

    private NodeContainer<? extends Node> getMatchingNodeContainer(String instance, String graph, String node, int index, Node n) {
        if(n instanceof FunctionalNode) {
            return new AbstractFunctionalNode(instance, graph,node,index) { @NotNull
            @Override public FunctionalNode getC() { return (FunctionalNode) n; } };
        }
        else if(n instanceof StreamNode) {
            return new AbstractStreamNode(instance, graph, node, index) { @NotNull
            @Override public StreamNode getC() { return (StreamNode) n; } };
        } else {
            // default, generic node
            return new AbstractNode<>(instance, graph, node, index) { @NotNull
            @Override public Node getC() { return n; } };
        }
    }


    private void parseNodeDependencies(@NotNull final ScrapeSpecification jobDefinition) throws IOException {
        if(jobDefinition.getDependencies().isPresent()) {
            File ndep = getFirstExistingPaths(jobDefinition.getDependencies().get(), jobDefinition.getPaths());

            Map<String, String> nodeDependencies = jobNodeDependencies.getOrDefault(jobDefinition, new LinkedHashMap<>());
            StringUtil.readBody(ndep, line -> {
                String node = line.split(":")[0];
                String version = line.split(":")[1];

                if (nodeDependencies.get(node) != null) log.log(WARNING,  "Node dependency already contained, overwriting: {0}", node);
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
                if(nodeConfig.get("type") == null) throw new IOException("Node has no field type");
                if(((String) nodeConfig.get("type")).equalsIgnoreCase("fragment")) {

                    // find fragment file
                    String location = (String) nodeConfig.get("required");
                    File fragment = getFirstExistingPaths(location, jobDefinition.getPaths());
                    log.log(DEBUG, "Pre-processing fragment {0}", location);

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
                log.log(DEBUG, "Pre-processing fragment {0}", location);

                // generate fragments
                List<Map<String, Object>> generated = generateFragmentRecursive(fragment, jobDefinition, location);

                // add all in order
                fragments.addAll(generated);
            } else {
                // not a fragment
//                node.put("fragment", fragmentName);
                fragments.add(node);
            }
        }

        return fragments;
    }

    @NotNull
    public List<AbstractMetadata> getPlugins() {
        return plugins.getPlugins();
    }

    @NotNull
    public String getNodes() {
        return plugins.nodeDiscovery();
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
                                          @NotNull final String type, @NotNull final String version) throws ValidationException {
        AbstractMetadata curr = null;

        for (AbstractMetadata processPlugin : processPlugins) {
            if(curr == null) {
                curr = processPlugin;
            } else {
                if(processPlugin.backwardsCompatible(curr)) curr = processPlugin;
            }
        }

        if(curr == null) {
            String msg = "No plugin for "+type+" (v"+version+") found! " +
                    "Provide an implementation with qualifying version number.";
            throw new ValidationException(msg);
        }
        return curr.getNode();
    }
}
