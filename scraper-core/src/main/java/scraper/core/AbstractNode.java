package scraper.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.EnsureFile;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.NodeException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.ControlFlowEdge;
import scraper.api.flow.FlowMap;
import scraper.api.flow.FlowState;
import scraper.api.flow.impl.ControlFlowEdgeImpl;
import scraper.api.node.Node;
import scraper.api.node.NodeAddress;
import scraper.api.node.NodeHook;
import scraper.api.node.NodeInitializable;
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
import java.util.function.Supplier;

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
    /** Comment is only used in the .scrape file to describe what the intent of the node is */
    @FlowKey
    protected String __comment;

    /** Decide log level threshold for this node */
    @FlowKey(defaultValue = "\"INFO\"")
    protected NodeLogLevel logLevel;
    /** Log statement to be printed */
    @FlowKey
    protected Template<String> log = new Template<>(){};

    /** Label of a node which can be used as a goto reference */
    @FlowKey
    protected String label;
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
    /** Fragment this node belongs to, if any */
    @FlowKey
    protected String fragment; // automatically set on processing

    /** If set, returns a thread pool with given name and {@link #threads} */
    @FlowKey(defaultValue = "\"main\"")
    protected String service;
    /** Number of worker threads for given executor service pool {@link #service} */
    @FlowKey(defaultValue = "25")
    protected @Argument
    Integer threads;

    /** All ensureFile fields of this node */
    private final ConcurrentMap<Field, EnsureFile> ensureFileFields = new ConcurrentHashMap<>();

    /**
     * Initializes the {@link #stageIndex} and all fields marked with {@link FlowKey}. Evaluates
     * actual values for fields marked with {@link Argument} with the initial argument map.
     *
     * @param job Job that this node belongs to
     * @throws ValidationException If a JSON parse error or a reflection error occurs
     */
    @Override
    public void init(ScrapeInstance job) throws ValidationException {
//        Runtime.getRuntime().addShutdownHook(new Thread(this::nodeShutdown));

        // set stage indices
        this.jobPojo = job;
        for (int i = 0; i < job.getJobProcess().size(); i++) {
            if(job.getJobProcess().get(i) == this) {
                this.stageIndex = i;
                break;
            }
        }

        // set logger name
        String number = String.valueOf(getJobPojo().getJobProcess().size());
        int indexLength = number.toCharArray().length;
        initLogger(indexLength);
        log(TRACE,"Start init {}", this);

        // initialize fields with arguments
        Set<String> expectedFields = initFields(job.getProcessKeys(stageIndex), job.getInitialArguments());

        // check actual fields against expected fields
        for (String actualField : job.getProcessKeys(getStageIndex()).keySet()) {
            if (!expectedFields.contains(actualField)) {
                log(WARN,"Found field defined in scrape jobPojo, but not expected in implementation: {}", actualField);
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

        if(jobPojo != null && jobPojo.getAll() != null) {
            allFields = jobPojo.getAll().get(getClass().getSimpleName());

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

    @Override
    public List<ControlFlowEdge> getOutput() {
        List<ControlFlowEdge> flows = new ArrayList<>();
        if(forward) {
            if(goTo != null)
                flows.add(new ControlFlowEdgeImpl(nameOf(goTo), " goto ", goTo));
            else {
                int i = getStageIndex();
                // if not last node: add simple forward
                if(getJobPojo().getJobProcess().size() != i+1) {
                    flows.add(new ControlFlowEdgeImpl(nameOf(""+(i+1))," forward ", String.valueOf(i+1)));
                }
            }
        }

        return flows;
    }

    @Override
    public List<ControlFlowEdge> getInput() {
        List<ControlFlowEdge> input = new ArrayList<>();
        if(stageIndex == 0) {
            input.add(new ControlFlowEdgeImpl("start", "", null));
        }

        for (int stageIndex = 0; stageIndex < jobPojo.getJobProcess().size(); stageIndex++) {
            List<ControlFlowEdge> output = jobPojo.getJobProcess().get(stageIndex).getOutput();
            for (ControlFlowEdge flow : output) {
                if(flow.getTarget().equalsIgnoreCase(getName()) && !flow.isDispatched()) {
                    input.add(new ControlFlowEdgeImpl(nameOf(""+stageIndex), " return ", ""+stageIndex));
                }
            }
        }

        return input;
    }

    @Override
    public String getName() {
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

    @Override public String nameOf(String target) { return getJobPojo().getProcessNode(target).getName(); }
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

    @Override
    public FlowMap forward(FlowMap o, @NotNull NodeAddress other) throws NodeException {
        // do nothing
        if(!getForward()) return o;

        // get target node
        Node target = null;
        if(other.getLabel() != null) {
            // assume node target is always valid (static checking)
            target = getJobPojo().getProcessNode(other.getLabel());
        }
        else if (getStageIndex() != getJobPojo().getJobProcess().size() - 1) {
            target = getJobPojo().getProcessNode(String.valueOf(getStageIndex() + 1));
        }

        // last node or no node target
        if(target == null) return o;

        return target.accept(o);
    }

    @Override
    public void forkDispatch(FlowMap o, NodeAddress target) {
        dispatch(() -> {
            try {
                return forward(o, target);
            } catch (NodeException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<FlowMap> forkDepend(FlowMap o, NodeAddress target) {
        return dispatch(() -> {
            try {
                return forward(o, target);
            } catch (NodeException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Object getKeySpec(final String argument) {
        return getJobPojo().getProcessKey(getStageIndex(), argument);
    }

    @Override
    public Map<String, Object> getNodeJsonSpec() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getType() {
        return this.type;
    }

    public String get__comment() {
        return this.__comment;
    }

    public NodeLogLevel getLogLevel() {
        return this.logLevel;
    }

    @Override
    public NodeAddress getAddress() {
        // TODO better
        return NodeUtil.addressOf(label);
    }

    public Boolean getForward() {
        return this.forward;
    }

    public String getGoTo() {
        return this.goTo;
    }

    public ScrapeInstance getJobPojo() {
        return this.jobPojo;
    }

    public int getStageIndex() {
        return this.stageIndex;
    }

    public String getFragment() {
        return this.fragment;
    }

    @Override
    public NodeAddress getTarget() {
        return NodeUtil.addressOf(goTo);
    }

    @Override
    public Collection<NodeHook> beforeHooks() {
        return Set.of(this::start);
    }

    @Override
    public Collection<NodeHook> afterHooks() {
        return Set.of(this::finish);
    }

}
