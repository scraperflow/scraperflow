package scraper.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import scraper.api.flow.ControlFlow;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.FlowMap;
import scraper.api.flow.FlowState;
import scraper.api.flow.impl.ControlFlowEdgeImpl;
import scraper.api.node.Node;
import scraper.api.node.NodeAddress;
import scraper.api.node.NodeHook;
import scraper.api.node.NodeInitializable;
import scraper.api.node.impl.NodeAddressImpl;
import scraper.api.specification.ScrapeInstance;
import scraper.util.NodeUtil;
import scraper.utils.ClassUtil;
import scraper.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static scraper.api.flow.impl.ControlFlowEdgeImpl.edge;
import static scraper.core.NodeLogLevel.*;
import static scraper.util.NodeUtil.infoOf;


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
public abstract class AbstractNode implements Node, NodeInitializable {
    /** Logger with the actual class name */
    protected Logger l = LoggerFactory.getLogger(getClass());

    /** Node type. Is used once to create an instance of an actual node implementation. */
    @FlowKey(mandatory = true)
    protected String type;
//    /** Comment is only used in the .scrape file to describe what the intent of the node is */
//    @FlowKey
//    protected String __comment;

    /** Decide log level threshold for this node */
    @FlowKey(defaultValue = "\"INFO\"")
    protected NodeLogLevel logLevel;
    /** Log statement to be printed */
    @FlowKey
    protected Template<String> log = new Template<>(){};

    /** Label of a node which can be used as a goto reference */
    @FlowKey
    protected NodeAddress address = new NodeAddressImpl();
    /** Indicates if forward has any effect or not. */
    @FlowKey(defaultValue = "true")
    protected Boolean forward;
    /** Target label */
    @FlowKey
    protected String goTo;

    /** Reference to its parent job */
    @JsonIgnore
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
    @JsonIgnore
    protected Map<String, Object> nodeConfiguration;

    /** Set during init of node */
    @JsonIgnore
    private NodeAddress graphKey;

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

        // set stage indices
        this.jobPojo = job;
        job.getGraphs().forEach((k, graph) -> {
            for (int i = 0; i < graph.size(); i++) {
                if(job.getGraph(k).get(i) == this) {
                    this.stageIndex = i;
                    break;
                }
            }
        });

        // set logger name
        String number = String.valueOf(job.getGraph(getGraphKey()).size());
        int indexLength = number.toCharArray().length;
        initLogger(indexLength);
        log(TRACE,"Start init {}", this);

        // initialize fields with arguments
        Set<String> expectedFields = initFields(getNodeConfiguration(), job.getInitialArguments());

        // check actual fields against expected fields
        for (String actualField : getNodeConfiguration().keySet()) {
            if (!expectedFields.contains(actualField)) {
                log(WARN,"Found field defined in flow, but not expected in implementation of node: {}", actualField);
            }
        }

        log(TRACE,"Finished init {}", this);
    }

    public Set<String> initFields(Map<String, Object> spec, Map<String,Object> initialArguments) throws ValidationException {
        // collect expected fields to check against
        Set<String> expectedFields = new HashSet<>();

        try { // ensure templated arguments
            List<Field> allFields = ClassUtil.getAllFields(new LinkedList<>(), getClass());

            for (Field field : allFields) {
                log(TRACE,"Initializing field {} of {}", field.getName(), this);

                FlowKey flowKey = field.getAnnotation(FlowKey.class);
                Argument ann = field.getAnnotation(Argument.class);

                if (flowKey != null) {
                    EnsureFile ensureFile = field.getAnnotation(EnsureFile.class);
                    if(ensureFile != null) ensureFileFields.put(field, ensureFile);

                    // save name for actual<->expected field comparison
                    expectedFields.add(field.getName());

                    // initialize field
                    initField(field, flowKey, ann, spec, initialArguments);
                }

            }


            log(TRACE,"Finished initializing fields for {}", this);
        }
        catch (IllegalAccessException e) {
            throw new ValidationException("Reflection not implemented correctly", e);
        }

        return expectedFields;
    }

    @NotNull
    public Map<String, Object> getNodeConfiguration() {
        return nodeConfiguration;
    }

    public void setNodeConfiguration(@NotNull Map<String, Object> configuration, @NotNull NodeAddress graphKey) {
        nodeConfiguration = configuration;
        this.graphKey = graphKey;
    }

    public void initLogger(int indexLength) {
        String loggerName =
                String.format("%s%"+indexLength+"s | %s",
                        StringUtil.cutTo(getJobPojo().getName()) + " > ",
                        stageIndex,
                        getClass().getSimpleName().substring(0, getClass().getSimpleName().length()-4));

        l = LoggerFactory.getLogger(loggerName);
    }

    protected ExecutorService getService(String defaultService) {
        return getJobPojo().getExecutors().getService(service, threads);
    }

    protected ExecutorService getService(String defaultService, Integer threads) {
        return getJobPojo().getExecutors().getService(service, threads);
    }

    /**
     * Initializes a field with its actual value. If it is a template, its value is evaluated with the given map.
     * @param field Field of the node to initialize
     * @param flowKey indicates optional value
     * @param ann Indicates a template field
     * @param args The input map
     * @throws ValidationException If there is a class mismatch between JSON and node definition
     * @throws IllegalAccessException If reflection is implemented incorrectly
     */
    private void initField(Field field,
                           FlowKey flowKey, Argument ann,
                           Map<String, Object> spec,
                           Map<String, Object> args)
            throws ValidationException, IllegalAccessException {
        // enable reflective access
        field.setAccessible(true);

        // this is the value which will get assigned to the field after evaluation
        Object value;
        Map<String, Object> allFields;
        Object jsonValue = spec.get(field.getName());
        Object globalValue = null;

        if(jobPojo != null && jobPojo.getGlobalNodeConfigurations() != null) {
            allFields = jobPojo.getGlobalNodeConfigurations().get(getClass().getSimpleName());

            // fetch global value, if any
            if(allFields != null) {
                Object globalKey = allFields.get(field.getName());
                if (globalKey != null) {
                    globalValue = globalKey;
                }
            }
        }

        try {
            value = NodeUtil.getValueForField(
                    field.getType(), field.get(this), jsonValue, globalValue,
                    flowKey.mandatory(), flowKey.defaultValue(),
                    ann != null, (ann != null ? ann.converter() : null),
                    args);
        } catch (ValidationException e){
            log(ERROR, "Bad field definition: '{}'", field.getName());
            throw e;
        }

        if(value != null) field.set(this, value);
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
        //TODO plugin preconditions

        // evaluate and write log message if any
        try {
            String logString = log.eval(map);
            if(logString != null) log(logLevel, logString);
        } catch (Exception e) {
            log(ERROR, "Could not evaluate log template: {}", e.getMessage());
        }

        // update FlowState
        updateFlowInfo(map, this);

        // ensure files exist
        try {
            for (Field ensureFileField : ensureFileFields.keySet()) {
                ensureFileField.setAccessible(true);

                String path;
                if(Template.class.isAssignableFrom(ensureFileField.getType())) {
                    Template<?> templ = (Template) ensureFileField.get(this);
                    path = (String) templ.eval(map);
                } else {
                    path = (String) ensureFileField.get(this);
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

    private void updateFlowInfo(FlowMap map, AbstractNode abstractNode) {
        FlowState newState = infoOf(map, abstractNode, getJobPojo().getName());
        map.setFlowState(newState);
    }

    /**
     * @param args The current map
     */
    protected void finish(final FlowMap args) {

    }

    /** Dispatches an action in an own thread, ignoring the result and possible exceptions. */
    protected CompletableFuture<FlowMap> dispatch(Supplier<FlowMap> o) {
        return dispatch(o, service);
    }

    protected CompletableFuture<FlowMap> dispatch(Supplier<FlowMap> o, String defaultService) {
        return CompletableFuture.supplyAsync(o, getJobPojo().getExecutors().getService(defaultService, threads));
    }

    // ------------------------
    // CONTROL FLOW FUNCTIONS
    // ------------------------

    // default implementation only is concerned with forward to next/goTo node
    @NotNull
    @Override
    public List<ControlFlowEdge> getOutput() {
        NodeAddress nextTarget = getGoTo();
        if(nextTarget == null) return List.of();

        Node nextNode = getJobPojo().getNode(nextTarget);
        if(forward) {
            if(goTo != null)
                return List.of(edge(getAddress(), nextNode.getAddress(), "goTo"));
            else
                return List.of(edge(getAddress(), nextNode.getAddress(), "forward"));
        }

        return List.of();
    }

    @NotNull
    @Override
    public List<ControlFlowEdge> getInput() {
        return getJobPojo().getGraph(getGraphKey())
                .stream()
                // every output of every node is checked
                .map(ControlFlow::getOutput)
                .flatMap(Collection::stream)
                // if target address is included, then the input to this node is that node
                .filter(edge -> edge.getToAddress().equals(getAddress()))
                // flip the from/to addresses for every filtered edge
                .map((Function<ControlFlowEdge, ControlFlowEdge>) origin ->
                        new ControlFlowEdgeImpl(origin.getToAddress(), origin.getFromAddress(), origin.getDisplayLabel(),
                                origin.isMultiple(), origin.isDispatched()))
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public String getDisplayName() {
        String name = getAddress().getLabel();
        if (!(name == null || name.isEmpty())) name += "\\n";
        else name = "";

        name += getClass().getSimpleName()+"@"+getStageIndex();

        return name;
    }

    protected void log(NodeLogLevel debug, String msg, Object... args) {
        log(l, logLevel, debug, msg, args);
    }

    // ----------------------------
    // GETTER AND UTILITY FUNCTIONS
    // ----------------------------

    @Override public String toString(){ return "["+getClass().getSimpleName()+"@"+getStageIndex()+"]"; }

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
        if(getGoTo() != null) {
            return jobPojo.getNode(getGoTo()).accept(o);
        }

        return o;
    }

    @NotNull
    @Override
    public FlowMap eval(@NotNull final FlowMap o, @NotNull final NodeAddress target) throws NodeException {
        return jobPojo.getNode(target).accept(o);
    }

    @Override
    public void forkDispatch(@NotNull final FlowMap o, @NotNull final NodeAddress target) {
        dispatch(() -> {
            try {
                return eval(o, target);
            } catch (NodeException e) {
                log(ERROR, "Fork dispatch to goTo '{}' terminated exceptionally. {}", target, e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<FlowMap> forkDepend(@NotNull final FlowMap o, @NotNull final NodeAddress target) {
        return dispatch(() -> {
            try {
                return eval(o, target);
            } catch (NodeException e) {
                log(ERROR, "Fork depend to goTo '{}' terminated exceptionally. {}", target, e);
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

    public int getStageIndex() {
        return this.stageIndex;
    }

    @Override
    public @Nullable NodeAddress getGoTo() {
        if(goTo != null) {
            return NodeUtil.addressOf(goTo);
        } else {
            return getJobPojo().getForwardTarget(getAddress());
        }
    }

    @NotNull
    @Override
    public NodeAddress getGraphKey() {
        return this.graphKey;
    }

    @NotNull @Override public String getGraph() { return this.graphKey.toString(); }

    @NotNull
    @Override
    public Collection<NodeHook> beforeHooks() {
        return Set.of(this::start);
    }

    @NotNull
    @Override
    public Collection<NodeHook> afterHooks() {
        return Set.of(this::finish);
    }

}
