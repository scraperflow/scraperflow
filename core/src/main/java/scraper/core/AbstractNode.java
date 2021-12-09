package scraper.core;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.Argument;
import scraper.annotations.EnsureFile;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.NodeIOException;
import scraper.api.ValidationException;
import scraper.api.FlowMap;
import scraper.api.Address;
import scraper.api.GraphAddress;
import scraper.api.NodeAddress;
import scraper.api.NodeContainer;
import scraper.api.NodeLogLevel;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.impl.GraphAddressImpl;
import scraper.api.node.impl.NodeAddressImpl;
import scraper.api.Node;
import scraper.api.NodeHook;
import scraper.api.T;
import scraper.api.ScrapeInstance;
import scraper.util.NodeUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.*;
import static scraper.api.NodeLogLevel.*;
import static scraper.util.NodeUtil.*;
import static scraper.utils.ClassUtil.getAllFields;


/**
 * Basic functionality of a Node with labeling and goTo support.
 */
@NodePlugin("1.0.2")
public abstract class AbstractNode<NODE extends Node> extends IdentityEvaluator implements NodeContainer<NODE> {
    /** Logger with the actual class name */
    protected System.Logger l;

    /** Node type. Is used once to create an instance of an actual node implementation. */
    @FlowKey(mandatory = true)
    protected String type;

    /** Log level threshold for this node */
    @FlowKey(defaultValue = "\"INFO\"")
    protected NodeLogLevel logLevel = INFO;

    /** Log statement to be printed */
    @FlowKey
    protected T<?> log = new T<>(){};

    /** Indicates if the flow continues after this node or not */
    @FlowKey(defaultValue = "true")
    protected Boolean forward;

    /** Target label */
    @FlowKey
//    @Flow(dependent = true, crossed = false) special case 1: forward contract
    protected Address goTo;

    /** If set and a node creates flows, the node uses a thread pool denoted by this name and <var>threads</var> */
    @FlowKey
    protected String service;

    /** Number of worker threads for given executor service pool <var>service</var> */
    @FlowKey(defaultValue = "100") @Argument
    protected Integer threads;

    /** Label of a node which can be used as a goTo reference */
    @FlowKey
    private String label;
    private NodeAddress absoluteAddress;

    /** Reference to its parent job */
    protected ScrapeInstance jobPojo;

    /** All ensureFile fields of this node */
    private final ConcurrentMap<Field, EnsureFile> ensureFileFields = new ConcurrentHashMap<>();

    /** Current node configuration */
    protected Map<String, ?> nodeConfiguration;

    /** Set during init of node */
    private GraphAddress graphKey;


    public AbstractNode(@NotNull String instance, @NotNull String graph, @Nullable String node, int index) {
        this.absoluteAddress = new NodeAddressImpl(instance, graph, node, index);
    }

    /**
     * Initializes the stageIndex and all fields marked with {@link FlowKey}. Evaluates
     * actual values for fields marked with {@link Argument} with the initial argument map.
     *
     * @param job Job that this node belongs to
     * @throws ValidationException If a JSON parse error or a reflection error occurs
     */
    @Override
    public void init(@NotNull final ScrapeInstance job) throws ValidationException {
//        Runtime.getRuntime().addShutdownHook(new Thread(this::nodeShutdown));
        this.jobPojo = job;

        initLogger();
        log(TRACE,"Start init {0}", this);

        // initialize fields with arguments
        try {
            initFields(this, getNodeConfiguration(),
                    job.getEntryArguments(), job.getSpecification().getGlobalNodeConfigurations()
                    );
            initFields(getC(), getNodeConfiguration(),
                    job.getEntryArguments(), job.getSpecification().getGlobalNodeConfigurations()
                    );

            // get ensure fields
            List<Field> test = getAllFields(new LinkedList<>(), getC().getClass());
            test.forEach(f -> {
                EnsureFile ensureFile = f.getAnnotation(EnsureFile.class);
                if(ensureFile != null) ensureFileFields.put(f, ensureFile);
            });

            ensureFiles(FlowMapImpl.origin(job.getEntryArguments()));

            log(TRACE,"Finished init {0}", this);

            // init node
            getC().init(this, job);
        } catch (Exception e) {
            log(ERROR, "Could not initialize field: {0}", e.getMessage());
            throw new ValidationException(e, "Could not initialize fields for " + getAddress() +": " + e.getMessage());
        }
    }

    @Override
    public List<ValidationException> initWithErrors(@NotNull final ScrapeInstance job) {
//        Runtime.getRuntime().addShutdownHook(new Thread(this::nodeShutdown));
        this.jobPojo = job;

        initLogger();
        log(TRACE,"Start init {0}", this);

        // initialize fields with arguments
        var errors1 = initFieldsWithErrors(this, getNodeConfiguration(),
                job.getEntryArguments(), job.getSpecification().getGlobalNodeConfigurations()
        );
        var errors2 = initFieldsWithErrors(getC(), getNodeConfiguration(),
                    job.getEntryArguments(), job.getSpecification().getGlobalNodeConfigurations()
            );

        // get ensure fields
        List<Field> test = getAllFields(new LinkedList<>(), getC().getClass());
        test.forEach(f -> {
            EnsureFile ensureFile = f.getAnnotation(EnsureFile.class);
            if(ensureFile != null) ensureFileFields.put(f, ensureFile);
        });

        ensureFiles(FlowMapImpl.origin(job.getEntryArguments()));

        log(TRACE,"Finished init {0}", this);

        return concat(of(errors1), of(errors2)).flatMap(Collection::stream).collect(Collectors.toList());
    }



    public void setNodeConfiguration(@NotNull Map<String, ?> configuration, @NotNull String instance, @NotNull String graphKey) {
        nodeConfiguration = configuration;
        this.graphKey = new GraphAddressImpl(instance, graphKey);
    }

    public void initLogger() {
        String loggerName = getAddress().toString();
        l = System.getLogger(loggerName);
    }

    /**
     * <ul>
     *     <li>Evaluates templates</li>
     *     <li>Ensures that each file described by an {@link EnsureFile} field exists</li>
     * </ul>
     *
     * @param map The current forwarded map
     */
    @SuppressWarnings("unused")
    public void start(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap map) {
        // evaluate and write log message if any
        try {
            if(log.getTerm() != null) {
                Object logString = log.getTerm().eval(map);
                if(logString != null) log(logLevel, logString.toString());
            }
        } catch (Exception e) {
            log(ERROR, "Could not evaluate log template: {0}", e.getMessage());
        }


        // ensure files exist
        ensureFiles(map);
    }

    private void ensureFiles(FlowMap map) {
        try {
            for (Field ensureFileField : ensureFileFields.keySet()) {
                ensureFileField.setAccessible(true);

                String path = null;
                if(T.class.isAssignableFrom(ensureFileField.getType())) {
                    T<?> templ = (T<?>) ensureFileField.get(getC());
                    Optional<?> maybePath = map.evalMaybe(templ);
                    if(maybePath.isPresent()) path = (String) maybePath.get();
                } else {
                    path = (String) ensureFileField.get(getC());
                }

                if (path == null) continue;

                log(TRACE,"Ensure file of field {0} at {1}", ensureFileField.getName(), path);
                if(ensureFileFields.get(ensureFileField).ensureDir())
                    getJobPojo().getFileService().ensureDirectory(new File(path));

                if(path.endsWith(File.separator)) {
                    getJobPojo().getFileService().ensureDirectory(new File(path+"."));
                } else {
                    getJobPojo().getFileService().ensureFile(path);
                }
            }

        } catch (IllegalAccessException | ClassCastException e) {
            throw new RuntimeException("Implemented reflection badly: ", e);
        } catch (IOException e) {
            log(ERROR,"Failed ensure file: {0}", e.getMessage());
            throw new NodeIOException("Failed ensuring directory or file: " + e.getMessage());
        }
    }

    /**
     * @param o The current map
     */
    @SuppressWarnings("unused") // unused
    protected void finish(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) {
    }


    /** Dispatches an action in an own thread, ignoring the result and possible exceptions. */
    @NotNull
    protected void dispatch(@NotNull FlowMap o, Address target) {
        NodeContainer<? extends Node> targetNode = getTarget(this.getAddress(), target, getJobInstance());
        // supply in target nodes thread pool
        CompletableFuture.supplyAsync(() -> {
            try {
                targetNode.getC().accept(targetNode, o.newFlow());
                return null;
            } catch (Exception e) {
                log(ERROR, "Node error occurred at {0}", e.getMessage());
                return null;
            }
        }, targetNode.getService());
    }


    public void log(@NotNull NodeLogLevel debug, @NotNull String msg, @NotNull Object... args) {
        log(l, logLevel, debug, msg, args);
    }

    // ----------------------------
    // GETTER AND UTILITY FUNCTIONS
    // ----------------------------


    // node shutdown
//    @Override
//    public void nodeShutdown() {
//        log(NodeLogLevel.DEBUG, "{0}@{1} stopped gracefully", getClass().getSimpleName(), stageIndex);
//    }


    public static void log(@NotNull System.Logger log, @NotNull NodeLogLevel threshold, @NotNull NodeLogLevel level,
                           @NotNull String msg, @NotNull Object... args) {
        switch (level){
            case TRACE:
                // only l if trace
                if(threshold != TRACE) return;
                log.log(System.Logger.Level.INFO, msg, args);
                return;
            case DEBUG:
                // only l if level is debug or higher
                if(threshold == TRACE || threshold == NodeLogLevel.DEBUG) log.log(System.Logger.Level.DEBUG, msg, args);
                return;
            case INFO:
                if(threshold == WARN || threshold == ERROR) return;
                log.log(System.Logger.Level.INFO, msg, args);
                return;
            case WARN:
                if(threshold == ERROR) return;
                log.log(System.Logger.Level.WARNING,  msg, args);
                return;
            case ERROR:
                log.log(System.Logger.Level.ERROR,   msg, args);
        }
    }


    @NotNull @Override
    public void forward(@NotNull final FlowMap o) {
        if (getGoTo().isPresent()) {
            NodeContainer<? extends Node> targetNode = getGoTo().get();
            o.nextSequence();
            if ((this.getServiceGroup() != null && targetNode.getServiceGroup() != null) && this.getServiceGroup().equalsIgnoreCase(targetNode.getServiceGroup())) {
                targetNode.getC().accept(targetNode, o);
            } else {
                forkDispatch(o, targetNode.getAddress());
            }
        }
    }

    @NotNull @Override
    public void fork(@NotNull final FlowMap o) {
        if(getGoTo().isPresent()) {
            forkDispatch(o, getGoTo().get().getAddress());
        }
    }

    @NotNull @Override
    public void forward(@NotNull final FlowMap o, @NotNull final Address target) {
        NodeContainer<? extends Node> opt = NodeUtil.getTarget(getAddress(), target, getJobInstance());
        o.nextSequence();
        if ((this.getServiceGroup() != null && opt.getServiceGroup() != null) && this.getServiceGroup().equalsIgnoreCase(opt.getServiceGroup())) {
            opt.getC().accept(opt, o);
        } else {
            forkDispatch(o, opt.getAddress());
        }
    }

    public void forkDispatch(@NotNull final FlowMap o, @NotNull final Address target) {
        NodeContainer<? extends Node> opt = NodeUtil.getTarget(getAddress(), target, getJobInstance());
        dispatch(o, opt.getAddress());
    }

    @Override
    public String getServiceGroup() {
        return service;
    };

    @Override @NotNull
    public Optional<NodeContainer<? extends Node>> getGoTo() {
        if(goTo == null) {
            if(isForward()) {
                // get forward, only forward can create optional.empty?
                Address nextNode = getAddress().nextIndex();
                return getJobInstance().getNode(nextNode);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(NodeUtil.getTarget( getAddress(), goTo, getJobInstance() ));
        }
    }


    //===========
    // getter
    //===========

    @NotNull @Override
    public ExecutorService getService() {
        if(service != null) {
            return getJobPojo().getExecutors().getService(getJobPojo().getName(), service+getAddress().toString(), threads);
        } else {
            return getJobPojo().getExecutors().getService(getJobPojo().getName(), getAddress().toString(), threads);
        }
    }

    @NotNull
    public ScrapeInstance getJobPojo() {
        if(jobPojo == null) throw new IllegalStateException("Node is not associated to a job");
        return this.jobPojo;
    }

    @NotNull @Override
    public Optional<?> getKeySpec(@NotNull String argument) {
        return Optional.ofNullable(nodeConfiguration.get(argument));
    }

    @NotNull
    public Map<String, ?> getNodeConfiguration() { return nodeConfiguration; }

    @NotNull @Override
    public GraphAddress getGraphKey() { return this.graphKey; }

    @Override @NotNull
    public Collection<NodeHook> hooks() {
        return concat(
                of(basicHook),
                getJobInstance().getHooks().stream()
        ).sorted().collect(Collectors.toList());
    }

    @Override
    public boolean isForward() { return forward; }

    @NotNull
    public String getType() { return this.type; }

    @NotNull
    public NodeLogLevel getLogLevel() { return this.logLevel; }

    @NotNull @Override
    public NodeAddress getAddress() { return absoluteAddress; }

    @NotNull
    public Boolean getForward() { return this.forward; }

    @NotNull @Override
    public ScrapeInstance getJobInstance() { return getJobPojo(); }

    @Override @NotNull
    public String toString(){ return getAddress().toString(); }

    @NotNull
    public System.Logger getL() { return l; }

    @NotNull
    @Override
    public Address addressOf(@NotNull String representation) {
        return NodeUtil.addressOf(representation);
    }

    private final NodeHook basicHook = new NodeHook() {
        @Override public void beforeProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) { start(n,o); }
        @Override public void afterProcess(@NotNull NodeContainer<? extends Node> n, @NotNull FlowMap o) { finish(n,o); }
    };
}
