package scraper.api.specification.impl;

import scraper.annotations.EnsureFile;
import scraper.annotations.NotNull;
import scraper.api.*;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.NodeContainer;
import scraper.api.NodeLogLevel;
import scraper.api.Node;
import scraper.api.NodeHook;
import scraper.api.DefaultVisitor;
import scraper.api.Primitive;
import scraper.api.T;
import scraper.api.ExecutorsService;
import scraper.api.FileService;
import scraper.api.HttpService;
import scraper.api.ProxyReservation;
import scraper.api.ScrapeInstance;
import scraper.api.ScrapeSpecification;
import scraper.core.IdentityEvaluator;
import scraper.util.NodeUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.Logger.*;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static scraper.utils.ClassUtil.getAllFields;

public class ScrapeInstaceImpl extends IdentityEvaluator implements ScrapeInstance {

    private @NotNull static final System.Logger log = System.getLogger("ScraperInstance");

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

    private final Collection<NodeHook> hooks = new HashSet<>();

    public ScrapeInstaceImpl(ScrapeSpecification spec) {
        this.spec = spec;
    }

    public void init() throws ValidationException {
        log.log(DEBUG,"Initializing graphs {0}", getName());
        Set<NodeContainer<?>> refs = new HashSet<>();
        for (Map.Entry<Address, NodeContainer<? extends Node>> e : routes.entrySet()) {
            if(!refs.contains(e.getValue())) {
                refs.add(e.getValue());
                e.getValue().init(this);
            }
        }
    }

    @Override
    public void initC() throws ValidationException {
        Set<NodeContainer<?>> refs = new HashSet<>();
        for (Map.Entry<Address, NodeContainer<? extends Node>> e : routes.entrySet()) {
            if(!refs.contains(e.getValue())) {
                refs.add(e.getValue());
                e.getValue().getC().init(e.getValue(), this);
            }
        }
    }

    @Override
    public List<ValidationException> initWithErrors() {
        log.log(DEBUG,"Initializing graphs {0}", getName());
        Set<NodeContainer<?>> refs = new HashSet<>();
        List<ValidationException> errors = new LinkedList<>();

        for (Map.Entry<Address, NodeContainer<? extends Node>> e : routes.entrySet()) {
            if(!refs.contains(e.getValue())) {
                refs.add(e.getValue());
                errors.addAll(e.getValue().initWithErrors(this));
            }
        }

        return errors;
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
    public Optional<NodeContainer<? extends Node>> getEntry() {
        return Optional.ofNullable(routes.get(entry));
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
    public Optional<NodeContainer<? extends Node>> getNode(@NotNull String targetRepresentation) {
        if(!targetRepresentation.contains("<") || !targetRepresentation.contains(">")) {
            return getNode(NodeUtil.addressOf(targetRepresentation));
        } else {
            // remove <> brackets for utility call
            targetRepresentation = targetRepresentation.substring(1, targetRepresentation.length()-1);
            return getNode(NodeUtil.addressOf(targetRepresentation));
        }
    }

    @Override
    public void addRoute(@NotNull Address address, @NotNull NodeContainer<? extends Node> nodeAbstractNode) {
        assert !routes.containsKey(address) || routes.get(address) == nodeAbstractNode:
                "Already added " + address+ "  "+routes.get(address)+" <=> "+nodeAbstractNode;
        routes.put(address, nodeAbstractNode);

        if(address instanceof NodeAddress) { // add representations with only index/name
            routes.put(((NodeAddress) address).getOnlyIndex(), nodeAbstractNode);
            if(((NodeAddress) address).getOnlyLabel().isPresent()) {
                routes.put(((NodeAddress) address).getOnlyLabel().get(), nodeAbstractNode);
            }
        }
    }

    @NotNull
    @Override
    public Map<String, Object> getEntryArguments() {
        return initialArguments;
    }

    @NotNull
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

    @NotNull
    @Override
    public Collection<NodeHook> getHooks() {
        return hooks;
    }

    public void setFileService(FileService fileService) { this.fileService = fileService; }

    public void validate() throws ValidationException {
        try {
            for (NodeContainer<? extends Node> node : getAllNodes()) {
                List<Field> test = getAllFields(new LinkedList<>(), node.getC().getClass());
                List<Field> test2 = getAllFields(new LinkedList<>(), node.getClass());

                // test addresses
                testAddressTargets(test, node.getC(), node.getAddress());
                testAddressTargets(test2, node, node.getAddress());

                List<String> expectedFields = Stream.concat(test.stream(), test2.stream()).map(Field::getName).collect(Collectors.toList());

                node.getNodeConfiguration().forEach((k,v) ->
                        {
                            if (!expectedFields.contains(k)) {
                                node.log(NodeLogLevel.ERROR, "Found field defined but not expected in implementation of node: {0} ", k );
                                throw new IllegalStateException(
                                        "Found field defined in "+node.getAddress()+", but not expected in implementation of node: "+ k
                                );
                            }
                        }
                );

            }
        } catch (Exception e){
            throw new ValidationException(e, "Could not validate instance: " + e.getMessage());
        }
    }

    private void testAddressTargets(List<Field> fields, Object node, NodeAddress origin) throws Exception {
        for (Field field : fields) {
            try {
                field.setAccessible(true);

                // if just an Address.class target raw value of field
                if (field.getType() == Address.class) {
                    Address address = (Address) field.get(node);
                    if (address != null)
                        NodeUtil.getTarget(origin, address, this);
                }


                // descend into template to get all Address.class targets
                if (field.getType() == T.class) {
                    T<?> t = (T<?>) field.get(node);
                    if (t == null)
                        throw new ValidationException("Fix implementation, T<> " +
                                "field was not created for " + node.getClass());

                    ScrapeInstance instance = this;
                    if (t.getTerm() != null) {
                        t.getTerm().accept(new DefaultVisitor<Void>() {
                            @Override
                            public Void visitPrimitive(@NotNull Primitive<?> primitive) {
                                Object address = primitive.eval(new FlowMapImpl());

                                if (address instanceof Address) {
                                    Address add = (Address) address;
                                    NodeUtil.getTarget(origin, add, instance);
                                }
                                return null;
                            }
                        });
                    }
                }
            } catch (Exception e) {
                log.log(Level.ERROR, "{0}:{1} failed: {2}", origin, field.getName(), e.getMessage());
                throw e;
            }
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
