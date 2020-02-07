package scraper.api.specification.impl;

import com.google.common.collect.Streams;
import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.api.exceptions.ValidationException;
import scraper.api.node.*;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.Node;
import scraper.api.plugin.NodeHook;
import scraper.api.reflect.T;
import scraper.api.service.ExecutorsService;
import scraper.api.service.FileService;
import scraper.api.service.HttpService;
import scraper.api.service.ProxyReservation;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.util.NodeUtil;
import scraper.util.TemplateUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static scraper.api.node.container.NodeLogLevel.ERROR;
import static scraper.utils.ClassUtil.getAllFields;

public class ScrapeInstaceImpl implements ScrapeInstance {

    private @NotNull static final Logger log = org.slf4j.LoggerFactory.getLogger("ScraperInstance");

    /** Name of the jobPojo */
    private @NotNull String name = "NoName";

    /** Initial input arguments */
    private final @NotNull Map<String, Object> initialArguments = new HashMap<>();

    /** Addressable instances */
    public final @NotNull Map<InstanceAddress, ScrapeInstance> importedInstances = new HashMap<>();

    private final Map<Address, NodeContainer<? extends Node>> routes = new HashMap<>();


    private ExecutorsService executors;
    private HttpService httpService;
    private ProxyReservation proxyReservation;
    private FileService fileService;

    private GraphAddress entry;

    private Collection<NodeHook> hooks = new HashSet<>();

    public ScrapeInstaceImpl(ScrapeSpecification spec) {
        this.spec = spec;
    }

    public void init() throws ValidationException {
        log.info("Initializing graphs '{}'", getName());
        for (Map.Entry<Address, NodeContainer<? extends Node>> e : routes.entrySet()) {
            e.getValue().init(this);
        }
    }

    private final ScrapeSpecification spec;

    @NotNull
    @Override
    public ScrapeSpecification getSpecification() { return spec; }


    @NotNull @Override
    public Map<InstanceAddress, ScrapeInstance> getImportedInstances() {
        return this.importedInstances;
    }

    @NotNull public String getName() {
        return this.name;
    }
    public void setName(@NotNull String name) { this.name = name; }

    @Override
    public void setEntry(@NotNull GraphAddress address, @NotNull NodeContainer<? extends Node> nn) {
        assert routes.containsKey(address);
        this.entry = address;
    }

    @NotNull
    @Override
    public NodeContainer<? extends Node> getEntry() {
        return routes.get(entry);
    }

    @NotNull
    @Override
    public NodeContainer<? extends Node> getNode(@NotNull NodeAddress target) {
        NodeContainer<? extends Node> node = routes.get(target);
        if(node == null) throw new IllegalStateException("Node address " + target + " should exist but does not.");
        return node;
    }

    @NotNull
    @Override
    public Optional<NodeContainer<? extends Node>> getNode(@NotNull Address target) {
        return Optional.ofNullable(routes.get(target));
    }

    @NotNull
    @Override
    public Map<GraphAddress, List<NodeContainer<? extends Node>>> getGraphs() {
        return getAllNodes().stream().collect(Collectors.groupingBy(NodeContainer::getGraphKey));

    }

    @Override
    public void addRoute(@NotNull Address address, @NotNull NodeContainer<? extends Node> nodeAbstractNode) {
        assert !routes.containsKey(address) || routes.get(address) == nodeAbstractNode: "Already added " + address+ "  "+routes.get(address)+" <=> "+nodeAbstractNode;
        routes.put(address, nodeAbstractNode);
    }

    @NotNull
    @Override
    public Map<String, Object> getEntryArguments() {
        return initialArguments;
    }

    @Override
    public Map<Address, NodeContainer<? extends Node>> getRoutes() {
        return routes;
    }

    // ================
    // Services
    @NotNull public ExecutorsService getExecutors() { return this.executors; }
    public void setExecutors(ExecutorsService executors) { this.executors = executors; }
    @NotNull public HttpService getHttpService() { return httpService; }
    public void setHttpService(HttpService httpService) { this.httpService = httpService; }
    @NotNull public ProxyReservation getProxyReservation() { return proxyReservation; }
    public void setProxyReservation(ProxyReservation proxyReservation) { this.proxyReservation = proxyReservation; }
    @NotNull public FileService getFileService() { return fileService; }

    @Override
    public Collection<NodeHook> getHooks() {
        return hooks;
    }

    public void setFileService(FileService fileService) { this.fileService = fileService; }

    // TODO implement this in a better way
    public void validate() throws ValidationException {
        try {
            for (NodeContainer<? extends Node> node : getAllNodes()) {
                List<Field> test = getAllFields(new LinkedList<>(), node.getC().getClass());
                List<Field> test2 = getAllFields(new LinkedList<>(), node.getClass());

                // test addresses
                testAddressTargets(test, node.getC(), node.getAddress());
                testAddressTargets(test2, node, node.getAddress());

                List<String> expectedFields = Streams.concat(test.stream(), test2.stream()).map(Field::getName).collect(Collectors.toList());

                node.getNodeConfiguration().forEach((k,v) ->
                        {
                            if (!expectedFields.contains(k)) {
                                node.log(ERROR, "Found field defined but not expected in implementation of node: {} ", k );
                                throw new IllegalStateException(
                                        "Found field defined in "+node.getAddress()+", but not expected in implementation of node: "+ k
                                );
                            }
                        }
                );

            }
        } catch (Exception e){
            throw new ValidationException(e, "Could not validate instance");
        }
    }

    private void testAddressTargets(List<Field> fields, Object node, NodeAddress origin) throws Exception {
        try {
            for (Field field : fields) {
                field.setAccessible(true);

                // if just an Address.class target raw value of field
                if(field.getType() == Address.class) {
                    Address address = (Address) field.get(node);
                    if(address != null)
                        NodeUtil.getTarget(origin, address, this);
                }


                // descend into template to get all Address.class targets
                if(field.getType() == T.class) {
                    T<?> t = (T<?>) field.get(node);
                    if(t == null)
                        throw new ValidationException("Fix implementation, T<> " +
                                "field was not created for " + node.getClass());

                    if(t.getParsedJson() != null) {
                        TemplateUtil.accept(t, Address.class, n -> {
                            if(n != null)
                                NodeUtil.getTarget(origin, n, this);
                        });
                    }
                }
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw e;
        }
    }

    public List<NodeContainer<? extends Node>> getAllNodes() {
        return routes.entrySet()
                .stream()
                .filter(e -> e.getKey() instanceof NodeAddress)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public void addHook(NodeHook h) { hooks.add(h); }
}
