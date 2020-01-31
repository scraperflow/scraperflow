package scraper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowStateImpl;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.NodeAddress;
import scraper.api.node.NodeHook;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.impl.GraphAddressImpl;
import scraper.api.node.impl.NodeAddressImpl;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static scraper.api.node.container.NodeLogLevel.*;
import static scraper.util.NodeUtil.initFields;
import static scraper.utils.ClassUtil.getAllFields;


/**
 * Basic abstract implementation of a Node with labeling and goTo support.
 * <p>
 * Provides following utility functions:
 * <ul>
 *     <li>Node factory method depending on the defined type</li>
 *     <li>Node coordination</li>
 *     <li>Argument evaluation</li>
 *     <li>Key reservation</li>
 *     <li>Ensure file</li>
 *     <li>Basic ControlFlow implementation</li>
 *     <li>Thread service pool management</li>
 * </ul>
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused"}) // abstract implementation
@NodePlugin("1.0.1")
public abstract class AbstractNode<NODE extends Node> implements NodeContainer<NODE> {
    /** Logger with the actual class name */
    protected Logger l = LoggerFactory.getLogger(getClass());

    /** Node type. Is used once to create an instance of an actual node implementation. */
    @FlowKey(mandatory = true)
    protected String type;

    /** Decide log level threshold for this node */
    @FlowKey(defaultValue = "\"INFO\"")
    protected NodeLogLevel logLevel;

    /** Log statement to be printed */
    @FlowKey
    protected T<Object> log = new T<>(){};

    /** Label of a node which can be used as a goto reference */
    @FlowKey
    protected final NodeAddress address;

    /** Indicates if forward has any effect or not. */
    @FlowKey(defaultValue = "true")
    protected Boolean forward;

    /** Target label */
    @FlowKey
    protected Address goTo;

    /** Reference to its parent job */
    protected ScrapeInstance jobPojo;

    /** Index of the node in the process list. Is set on init. */
    protected int stageIndex;

    /** If set, returns a thread pool with given name and {@link #threads} */
    @FlowKey(defaultValue = "\"main\"")
    protected String service;
    /** Number of worker threads for given executor service pool {@link #service} */
    @FlowKey(defaultValue = "25") @Argument
    protected Integer threads;

    /** All ensureFile fields of this node */
    private final ConcurrentMap<Field, EnsureFile> ensureFileFields = new ConcurrentHashMap<>();

    /** Current node configuration */
    protected Map<String, Object> nodeConfiguration;

    /** Set during init of node */
    private GraphAddress graphKey;

//    /** Target if a dispatched flow exception occurs */
//    @FlowKey
//    protected Address onForkException;

    public AbstractNode(String instance, String graph, String node, int index) {
        this.address = new NodeAddressImpl(instance, graph, node, index);
    }

    /**
     * Initializes the {@link #stageIndex} and all fields marked with {@link FlowKey}. Evaluates
     * actual values for fields marked with {@link Argument} with the initial argument map.
     *
     * @param job Job that this node belongs to
     * @throws ValidationException If a JSON parse error or a reflection error occurs
     */
    @Override
    public void init(@NotNull final ScrapeInstance job) throws ValidationException {
//        Runtime.getRuntime().addShutdownHook(new Thread(this::nodeShutdown));
        this.jobPojo = job;

        // set logger name
        String number = String.valueOf(job.getGraph(getGraphKey()).size());
        int indexLength = number.toCharArray().length;
        initLogger(indexLength);
        log(TRACE,"Start init {}", this);

        // initialize fields with arguments
        Set<String> expectedFields = initFields(this, getNodeConfiguration(), job.getInitialArguments(), job.getGlobalNodeConfigurations());
        Set<String> expectedField = initFields(getC(), getNodeConfiguration(), job.getInitialArguments(), job.getGlobalNodeConfigurations());

        // get ensure fields
        List<Field> test = getAllFields(new LinkedList<>(), getC().getClass());
        test.forEach(f -> {
                EnsureFile ensureFile = f.getAnnotation(EnsureFile.class);
                if(ensureFile != null) ensureFileFields.put(f, ensureFile);
        });

        // check actual fields against expected fields
        for (String actualField : getNodeConfiguration().keySet()) {
            if (!expectedFields.contains(actualField)) {
                log(WARN,"Found field defined in flow, but not expected in implementation of node: {}", actualField);
            }
        }

        log(TRACE,"Finished init {}", this);

        // init node
        getC().init(this, job);
    }


    @NotNull
    public Map<String, Object> getNodeConfiguration() {
        return nodeConfiguration;
    }

    public void setNodeConfiguration(@NotNull Map<String, Object> configuration, @NotNull String instance, @NotNull String graphKey) {
        nodeConfiguration = configuration;
        this.graphKey = new GraphAddressImpl(instance, graphKey);
    }

    public void initLogger(int indexLength) {
        String loggerName = getAddress().toString();
//                        getClass().getSimpleName().substring(0, getClass().getSimpleName().length()-4));
//        String loggerName =
//                String.format("%s > %s%"+indexLength+"s | %s",
//                        getJobPojo().getName(),
//                        getAddress().getLabel() + " @ ",
//                        getAddress().getIndex(),
//                        getClass().getSimpleName().substring(0, getClass().getSimpleName().length()-4));

        l = LoggerFactory.getLogger(loggerName);
    }

    @Override
    public ExecutorService getService() {
        return getJobPojo().getExecutors().getService(getJobPojo().getName(),service, threads);
    }


    /**
     * <ul>
     *     <li>Evaluates templates</li>
     *     <li>Ensures that each file described by an {@link EnsureFile} field exists</li>
     * </ul>
     *
     * @param map The current forwarded map
     */
    protected void start(FlowMap map) throws NodeException {
        // update FlowState
        updateFlowInfo(map, this, "start");

        // evaluate and write log message if any
        try {
            Object logString = Template.eval(log, map);
            if(logString != null) log(logLevel, logString.toString());
        } catch (Exception e) {
            log(ERROR, "Could not evaluate log template: {}", e.getMessage());
        }


        // FIXME re-implement
        // ensure files exist
        try {
            for (Field ensureFileField : ensureFileFields.keySet()) {
                ensureFileField.setAccessible(true);

                String path;
                if(T.class.isAssignableFrom(ensureFileField.getType())) {
                    T<?> templ = (T) ensureFileField.get(getC());
                    path = (String) map.eval(templ);
                } else {
                    path = (String) ensureFileField.get(getC());
                }

                if (path == null) continue; //TODO check if optional // TODO what does the TODO mean

                log(TRACE,"Ensure file of field {} at {}", ensureFileField.getName(), path);
                if(ensureFileFields.get(ensureFileField).ensureDir())
                    getJobPojo().getFileService().ensureDirectory(new File(path));

                if(path.endsWith(File.separator)) {
                    getJobPojo().getFileService().ensureDirectory(new File(path+"."));
                } else {
                    getJobPojo().getFileService().ensureFile(path);
                }
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException("Implemented reflection badly: ", e);
        } catch (IOException e) {
            log(ERROR,"Failed ensure file: {}", e.getMessage());
            throw new NodeException("Failed ensuring directory or file: " + e.getMessage());
        }
    }

    /**
     * @param o The current map
     */
    protected void finish(final FlowMap o) {
        updateFlowInfo(o, this, "finish");
    }

    private void updateFlowInfo(FlowMap map, AbstractNode abstractNode, String phase) {
        if(logLevel.worseOrEqual(WARN)) {
            // only history of start is tracked
            if(phase.equalsIgnoreCase("start")) return;
        }

        FlowStateImpl state = new FlowStateImpl(phase);
        state.log("address", getAddress());
        state.log("graph", getGraphKey());

        if(logLevel.worseOrEqual(INFO)) {
            state.log("keys", new HashSet<>(map.keySet()));
        }

        if(logLevel.worseOrEqual(DEBUG)) {
            Map<String, Object> keyValues = new HashMap<>();

            for (String key : map.keySet()) {
                if(map.get(key) instanceof String) {
                    // trim string
                    keyValues.put(key, ((String) Objects.requireNonNull(map.get(key)))
                            .substring(0, Math.min(100, ((String) Objects.requireNonNull(map.get(key))).length())));
                } else {
                    // else just put class name
                    keyValues.put(key, Objects.requireNonNull(map.get(key)).getClass().getName());
                }
            }
            state.log("key-values", keyValues);
        }

        // TODO implement TRACE deep copy keys
    }

    /** Dispatches an action in an own thread, ignoring the result and possible exceptions. */
    protected CompletableFuture<FlowMap> dispatch(Supplier<FlowMap> o) {
        return CompletableFuture.supplyAsync(o, getService());
    }


    public void log(NodeLogLevel debug, String msg, Object... args) {
        log(l, logLevel, debug, msg, args);
    }

    // ----------------------------
    // GETTER AND UTILITY FUNCTIONS
    // ----------------------------

    @Override public String toString(){ return getAddress().toString(); }

    // node shutdown
//    @Override
//    public void nodeShutdown() {
//        log(NodeLogLevel.DEBUG, "{}@{} stopped gracefully", getClass().getSimpleName(), stageIndex);
//    }

    public Logger getL() {
        return l;
    }

    public static void log(Logger log, NodeLogLevel threshold, NodeLogLevel level, String msg, Object... args) {
        switch (level){
            case TRACE:
                // only l if trace
                if(threshold != TRACE) return;
                log.info(msg, args);
                return;
            case DEBUG:
                // only l if level is debug or higher
                if(threshold == TRACE || threshold == NodeLogLevel.DEBUG) log.debug(msg, args);
                return;
            case INFO:
                if(threshold == WARN || threshold == ERROR) return;
                log.info(msg, args);
                return;
            case WARN:
                if(threshold == ERROR) return;
                log.warn(msg, args);
                return;
            case ERROR:
                log.error(msg, args);
        }
    }


    @NotNull
    @Override
    public FlowMap forward(@NotNull final FlowMap o) throws NodeException {
        // do nothing
        if(!getForward()) return o;
        Optional<NodeAddress> forwardTarget = getGoTo();

        if (forwardTarget.isPresent()) {
            Optional<NodeContainer<? extends Node>> targetNode = jobPojo.getNodeAbsolute(forwardTarget.get());
            // TODO
            return targetNode.get().getC().accept(targetNode.get(), o);
        }

        return o;
    }

    @NotNull
    @Override
    public FlowMap eval(@NotNull final FlowMap o, @NotNull final NodeAddress target) throws NodeException {
        Optional<NodeContainer<? extends Node>> targetNode = jobPojo.getNodeAbsolute(target);
        // TODO optional
        return targetNode.get().getC().accept(targetNode.get(), o);
    }

    @Override
    public void forkDispatch(@NotNull final FlowMap o, @NotNull final NodeAddress target) {
        dispatch(() -> {
            try {
                return eval(o, target);
            } catch (Exception e) {
                log(ERROR, "Dispatch terminated exceptionally {}: {}", target, e);
                // TODO re-add exception feature
//                if(onForkException != null) {
//                    try {
//                        return eval(o, onForkException);
//                    } catch (NodeException ex) {
//                        log(ERROR, "OnException fork target terminated exceptionally.", target, e);
//                        throw new RuntimeException(e);
//                    }
//                } else {
                    log(ERROR, "Fork dispatch to goTo '{}' terminated exceptionally.", target, e);
                    throw new RuntimeException(e);
//                }
            }
        });
    }

    @NotNull
    @Override
    public CompletableFuture<FlowMap> forkDepend(@NotNull final FlowMap o, @NotNull final NodeAddress target) {
        return dispatch(() -> {
            try {
                return eval(o, target);
            } catch (Exception e) {
                log(ERROR, "Fork depend to goTo '{}' terminated exceptionally.", target, e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Object getKeySpec(@NotNull final String argument) {
        return nodeConfiguration.get(argument);
    }

    public String getType() {
        return this.type;
    }

    public NodeLogLevel getLogLevel() {
        return this.logLevel;
    }

    @NotNull
    @Override
    public NodeAddress getAddress() {
        return address;
    }

    public Boolean getForward() {
        return this.forward;
    }

    public ScrapeInstance getJobPojo() {
        if(jobPojo == null) throw new IllegalStateException("Node is not associated to a job");
        return this.jobPojo;
    }

    @Override
    public ScrapeInstance getJobInstance() {
        return getJobPojo();
    }

    public int getStageIndex() {
        return this.stageIndex;
    }

    @Override
    public @NotNull Optional<NodeAddress> getGoTo() {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public GraphAddress getGraphKey() {
        return this.graphKey;
    }

    @Override
    @NotNull
    public Collection<NodeHook> beforeHooks() {
        return Set.of(this::start);
    }

    @Override
    @NotNull
    public Collection<NodeHook> afterHooks() {
        return Set.of(this::finish);
    }

    @Override
    public boolean isForward() {
        return forward;
    }

}
