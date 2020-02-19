package scraper.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.node.Argument;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.container.NodeLogLevel;
import scraper.api.node.impl.GraphAddressImpl;
import scraper.api.node.impl.NodeAddressImpl;
import scraper.api.node.type.Node;
import scraper.api.plugin.NodeHook;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;
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
@NodePlugin("1.0.2")
public abstract class AbstractNode<NODE extends Node> extends IdentityEvaluator implements NodeContainer<NODE> {
    /** Logger with the actual class name */
    protected Logger l = LoggerFactory.getLogger(getClass());

    /** Node type. Is used once to create an instance of an actual node implementation. */
    @FlowKey(mandatory = true)
    protected String type;

    /** Decide log level threshold for this node */
    @FlowKey(defaultValue = "\"INFO\"")
    protected NodeLogLevel logLevel = INFO;

    /** Log statement to be printed */
    @FlowKey
    protected T<Object> log = new T<>(){};

    /** Indicates if forward has any effect or not. */
    @FlowKey(defaultValue = "true")
    protected Boolean forward;

    /** Target label */
    @FlowKey protected Address goTo;

    /** If set, returns a thread pool with given name and {@link #threads} */
    @FlowKey
    protected String service;

    /** Number of worker threads for given executor service pool {@link #service} */
    @FlowKey(defaultValue = "100") @Argument
    protected Integer threads;

    /** Label of a node which can be used as a goto reference */
    @FlowKey // not used for anything at the moment
    private String label;
    private NodeAddress absoluteAddress;

    /** Reference to its parent job */
    protected ScrapeInstance jobPojo;

    /** Index of the node in the process list. Is set on init. */
    protected int stageIndex;

    /** All ensureFile fields of this node */
    private final ConcurrentMap<Field, EnsureFile> ensureFileFields = new ConcurrentHashMap<>();

    /** Current node configuration */
    protected Map<String, ?> nodeConfiguration;

    /** Set during init of node */
    private GraphAddress graphKey;

    /** Target if a dispatched flow exception occurs */
//    @FlowKey
//    protected Address onForkException;

    public AbstractNode(@NotNull String instance, @NotNull String graph, @Nullable String node, int index) {
        this.absoluteAddress = new NodeAddressImpl(instance, graph, node, index);
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

//        // set logger name
//        String number = String.valueOf(job.getGraph(getGraphKey()).size());
//        int indexLength = number.toCharArray().length;
//        initLogger(indexLength);
        initLogger();
        log(TRACE,"Start init {}", this);

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

            log(TRACE,"Finished init {}", this);

            // init node
            getC().init(this, job);
        } catch (Exception e) {
            log(ERROR, "Could not initialize field: '{}'", e.getMessage());
            throw new ValidationException(e, "Could not initialize fields for " + getAddress() +": " + e.getMessage());
        }
    }



    public void setNodeConfiguration(@NotNull Map<String, ?> configuration, @NotNull String instance, @NotNull String graphKey) {
        nodeConfiguration = configuration;
        this.graphKey = new GraphAddressImpl(instance, graphKey);
    }

    public void initLogger() {
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

    /**
     * <ul>
     *     <li>Evaluates templates</li>
     *     <li>Ensures that each file described by an {@link EnsureFile} field exists</li>
     * </ul>
     *
     * @param map The current forwarded map
     */
    protected void start(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap map) throws NodeException {
        // evaluate and write log message if any
        try {
            Object logString = Template.eval(log, map);
            if(logString != null) log(logLevel, logString.toString());
        } catch (Exception e) {
            log(ERROR, "Could not evaluate log template: {}", e.getMessage());
        }


        // ensure files exist
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

                log(TRACE,"Ensure file of field {} at {}", ensureFileField.getName(), path);
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
            log(ERROR,"Failed ensure file: {}", e.getMessage());
            throw new NodeException("Failed ensuring directory or file: " + e.getMessage());
        }
    }

    /**
     * @param o The current map
     */
    protected void finish(@NotNull NodeContainer<? extends Node> n, @NotNull final FlowMap o) {
        o.nextSequence();
    }


    /** Dispatches an action in an own thread, ignoring the result and possible exceptions. */
    @NotNull
    protected CompletableFuture<FlowMap> dispatch(@NotNull Supplier<FlowMap> o) {
        return CompletableFuture.supplyAsync(o, getService());
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
//        log(NodeLogLevel.DEBUG, "{}@{} stopped gracefully", getClass().getSimpleName(), stageIndex);
//    }


    public static void log(@NotNull Logger log, @NotNull NodeLogLevel threshold, @NotNull NodeLogLevel level,
                           @NotNull String msg, @NotNull Object... args) {
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


    @NotNull @Override
    public FlowMap forward(@NotNull final FlowMap o) throws NodeException {
        if (getGoTo().isPresent()) {
            NodeContainer<? extends Node> targetNode = getGoTo().get();
            return targetNode.getC().accept(targetNode, o);
        } else {
            return o;
        }
    }

    @NotNull @Override
    public FlowMap eval(@NotNull final FlowMap o, @NotNull final Address target) throws NodeException {
        NodeContainer<? extends Node> opt = NodeUtil.getTarget(getAddress(), target, getJobInstance());
        return opt.getC().accept(opt, o);
    }

    @Override
    public void forkDispatch(@NotNull final FlowMap o, @NotNull final Address target) {
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
//                    log(ERROR, "Fork dispatch to goTo '{}' terminated exceptionally.", target, e);
                    throw new RuntimeException(e);
//                }
            }
        });
    }

    @NotNull @Override
    public CompletableFuture<FlowMap> forkDepend(@NotNull final FlowMap o, @NotNull final Address target) {
        return dispatch(() -> {
            try {
                return eval(o, target);
            } catch (Exception e) {
                log(ERROR, "Fork depend to goTo '{}' terminated exceptionally.", target, e);
                throw new RuntimeException(e);
            }
        });
    }

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
            return getJobPojo().getExecutors().getService(getJobPojo().getName(), service, threads);
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
        return Stream.concat(
                Stream.of(basicHook),
                getJobInstance().getHooks().stream()
        ).collect(Collectors.toList());
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
    public Logger getL() { return l; }

    public int getStageIndex() { return this.stageIndex; }

    @Override
    public Address addressOf(String representation) {
        return NodeUtil.addressOf(representation);
    }

    private final NodeHook basicHook = new NodeHook() {
        @Override public void accept(NodeContainer<? extends Node> n, FlowMap o) throws NodeException { start(n,o); }
        @Override public void acceptAfter(NodeContainer<? extends Node> n, FlowMap o) { finish(n,o); }
    };
}
