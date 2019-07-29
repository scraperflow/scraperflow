package scraper.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.plugin.metadata.PluginMetadata;
import org.springframework.plugin.metadata.SimplePluginMetadata;
import scraper.annotations.NotNull;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.Node;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
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

import static scraper.utils.CollectionUtil.newAppend;

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


    private ScrapeInstaceImpl parseJob(ScrapeSpecification def) throws IOException {
        File f = locateScrapeFile(def);

        ScrapeInstaceImpl job = objectMapper.readValue(f, ScrapeInstaceImpl.class);
        if(def.getName() != null) job.setName(def.getName());

        job.setExecutors(executorsService);
        job.setFileService(fileService);
        job.setHttpService(httpService);
        job.setProxyReservation(proxyReservation);

        return job;
    }


    private Map<String,String> parseInputArguments(File args) throws IOException {
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

    public ScrapeInstaceImpl createEmptyJob() {
        ScrapeInstaceImpl job = new ScrapeInstaceImpl();

        job.setExecutors(executorsService);
        job.setFileService(fileService);
        job.setHttpService(httpService);
        job.setProxyReservation(proxyReservation);

        return job;
    }

    public ScrapeInstaceImpl convertScrapeJob(final ScrapeSpecification jobDefinition) throws IOException, ValidationException {
        return convertJob(jobDefinition, null);
    }

    // stest not implemented yet
//    public ScrapeInstaceImpl convertTestJob(final ScrapeSpecification jobDefinition,
//                                            final Function<String, Map<String, Object>> copySupplier) throws IOException, ValidationException {
//        return convertJob(jobDefinition, copySupplier);
//    }

    private ScrapeInstaceImpl convertJob(final ScrapeSpecification jobDefinition,
                                         Function<String, Map<String, Object>> nodeSupplier) throws IOException, ValidationException {
        // ===
        // Node dependencies
        // ===
        if (jobDefinition.getNodeDependencyFile() != null) {
            try {
                parseNodeDependencies(jobDefinition);
                log.info("Using node dependency file for job '{}': {}",
                        jobDefinition.getScrapeFile(),
                        jobDefinition.getNodeDependencyFile());
            } catch (FileNotFoundException e) {
                log.warn("Missing node dependency file: '{}'. Continuing without node versioning.",
                        jobDefinition.getNodeDependencyFile());
            }
        }

        // ===
        // Job Pojo
        // ===
        log.info("Parsing {}",jobDefinition.getScrapeFile());
        ScrapeInstaceImpl job = parseJob(jobDefinition);

        // ===
        // .args files
        // ===

        // first args from definition
        Map<String, Object> combinedArgs = new HashMap<>(job.initialArguments);

        // implied args
        try {
            String impliedArgs = Paths.get(FileUtil.replaceFileExtension(jobDefinition.getScrapeFile(), "args")).toString();
            File impliedArgsFile = findArgsFile(impliedArgs, jobDefinition);
            log.info("Parsing implied args file: {}", impliedArgs);
            Map<String, String> init = parseInputArguments(impliedArgsFile);
            combinedArgs.putAll(init);
        } catch (FileNotFoundException ignored) {}

        // later args files overwrite earlier ones
        for (String arg : jobDefinition.getArgumentFiles()) {
            File argsFile = findArgsFile(arg, jobDefinition);
            log.info("Parsing args file: {}", arg);
            Map<String, String> init = parseInputArguments(argsFile);
            combinedArgs.putAll(init);
        }

        job.setInitialArguments(combinedArgs);

        // ===
        // Pre-process fragments
        // ===
        log.info("Pre process fragments");
        preprocessFragments(jobDefinition, job);


        // ===
        // Process nodes and instantiate actual matching implementations
        // ===
        for (int index = 0; index < job.getProcess().size(); index++) {
            String nodeType = (String) job.getProcessKey(index, "type");

            String vers = jobNodeDependencies
                    .getOrDefault(jobDefinition, new LinkedHashMap<>())
                    .getOrDefault(nodeType, "0.0.0");
            PluginMetadata metadata = new SimplePluginMetadata(nodeType, vers);

            List<? extends AbstractMetadata> processPlugins = plugins.getPlugins().getPluginsFor(metadata);
            Node n = getHighestMatchingPlugin(processPlugins, metadata);
            job.addProcessNode(n);
        }

        job.init();

        return job;
    }


    private void parseNodeDependencies(ScrapeSpecification jobDefinition) throws IOException {
        File ndep = findNdepFile(jobDefinition);

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



    private void preprocessFragments(ScrapeSpecification jobDefinition, ScrapeInstaceImpl job) throws IOException {
        boolean fragmentFound;
        do {
            fragmentFound = false;

            Iterator<Map<String, Object>> iter = job.getProcess().iterator();
            int i = 0;

            // iterate over all current process nodes in the definition
            while (iter.hasNext()) {
                iter.next();

                // check if node is a fragment
                if(((String) job.getProcessKey(i, "type")).equalsIgnoreCase("fragment")) {

                    // find fragment file
                    String location = (String) job.getProcessKey(i, "required");
                    File fragment = findFragment(location, jobDefinition);

                    List<Map<String, Object>> replaced = generateFragmentRecursive(fragment, jobDefinition, location);

                    iter.remove();

                    for (int i1 = 0; i1 < replaced.size(); i1++) {
                        job.getProcess().add(i+i1, replaced.get(i1));
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
                File fragment = findFragment(location, jobDefinition);

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

    private File locateScrapeFile(ScrapeSpecification def) throws FileNotFoundException {
        return FileUtil.getFirstExisting(def.getScrapeFile(), newAppend(def.getPaths(), def.getBasePath()));
    }

    private File findArgsFile(String args, ScrapeSpecification job) throws FileNotFoundException {
        return FileUtil.getFirstExisting(args, newAppend(job.getPaths(), job.getBasePath()));
    }

    private File findNdepFile(ScrapeSpecification job) throws FileNotFoundException {
        String ndepLocation = StringUtil.removeExtension(job.getScrapeFile()).concat(".ndep");
        return FileUtil.getFirstExisting(ndepLocation, newAppend(job.getPaths(), job.getBasePath()));
    }

    private File findFragment(String location, ScrapeSpecification jobDefinition) throws FileNotFoundException {
        Collection<String> paths = StringUtil.pathProduct(jobDefinition.getPaths(), jobDefinition.getFragmentFolders());
        String parent = FileUtil.getParentPath(jobDefinition.getScrapeFile(), "").toString();
        List<String> candidates = newAppend((List<String>) new LinkedList<>(paths), new String[]{jobDefinition.getBasePath(), parent});
        return FileUtil.getFirstExisting(location, candidates);
    }
}
