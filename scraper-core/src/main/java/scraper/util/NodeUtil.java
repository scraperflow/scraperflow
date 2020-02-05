package scraper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.core.converter.StringToClassConverter;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.node.Address;
import scraper.api.node.GraphAddress;
import scraper.api.node.InstanceAddress;
import scraper.api.node.NodeAddress;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.impl.AddressImpl;
import scraper.api.node.impl.GraphAddressImpl;
import scraper.api.node.impl.InstanceAddressImpl;
import scraper.api.node.impl.NodeAddressImpl;
import scraper.api.node.type.Node;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;
import scraper.utils.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class NodeUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Evaluates the base template with the given map. Does nothing if base is not of type String.
     */
    private static Object replaceArguments(@NotNull Object base, Map<String, Object> o){
        if(!(base instanceof String)) return base;

        String replaced = (String) base;

        for (String arg : o.keySet()) {
            try {
                String ik = String.valueOf(o.get(arg));
                Pattern argsPattern = Pattern.compile("\\{('(.*?)')?"+Pattern.quote(arg)+"('(.*?)')?}");
                Matcher argsMatcher = argsPattern.matcher(replaced);
                if(o.get(arg) == null) {
                    replaced = argsMatcher.replaceAll("");
                } else {
                    replaced = argsMatcher.replaceAll("$2"+Matcher.quoteReplacement(ik)+"$4");
                }
            } catch (ClassCastException ignored) {}
        }

        return replaced;
    }

    public static FlowMap flowOf(Map<String, Object> initialArguments) {
        return FlowMapImpl.of(initialArguments);
    }

    public static FlowMap flowOf(FlowMap o) {
        return FlowMapImpl.copy(o);
    }

    public static boolean representationEquals(@NotNull String address1, @NotNull String address2) {
        if(address1.equalsIgnoreCase(address2)) return true;

        Pattern p = Pattern.compile("(\\w+\\.\\w+\\.)([\\w]+):(\\d+)");

        boolean a1isAbsolute = p.matcher(address1).results().findFirst().isPresent();
        boolean a2isAbsolute = p.matcher(address2).results().findFirst().isPresent();

        if (a1isAbsolute && test(address1, address2, p)) return true;
        if (a2isAbsolute && test(address2, address1, p)) return true;

        return false;
    }

    private static boolean test(@NotNull String address1, @NotNull String address2, Pattern p) {
        String onlyNodeRepresentation = p.matcher(address1).results().map(m -> m.group(1) + m.group(2)).findFirst().get();
        if(representationEquals(onlyNodeRepresentation, address2)) return true;

        String onlyIndexRepresentation = p.matcher(address1).results().map(m -> m.group(1) + m.group(3)).findFirst().get();
        if(representationEquals(onlyIndexRepresentation, address2)) return true;

        return false;
    }

    public static int representationHashCode(String representation) {
        Pattern p = Pattern.compile("(\\w+)(\\.\\w+)?(\\..*+)?");
        assert p.matcher(representation).results().findFirst().isPresent();

        // only hash instance + graph or only instance
        return Objects.hash(p.matcher(representation).results().findFirst().map(m -> m.group(1)).get());
    }

    @NotNull
    public static Address addressOf(String label) {
        if(label.contains(":")) throw new IllegalArgumentException("Forbidden to create absolute address");
        return new AddressImpl(label);
    }

    @NotNull
    public static GraphAddress addressOf(String instance, String graph) {
        return new GraphAddressImpl(instance, graph);
    }

    @NotNull
    public static NodeAddress addressOf(String instance, String graph, String node) {
        return new NodeAddressImpl(instance, graph, node, null);
    }

    @NotNull
    public static NodeAddress addressOf(String instance, String graph, Integer index) {
        return new NodeAddressImpl(instance, graph, null, index);
    }


    public static void initFields(Object instance, Map<String, ?> spec, Map<String,Object> initialArguments, Map<String, Map<String, Object>> globalConfigurations) throws ValidationException {
        List<Field> allFields = ClassUtil.getAllFields(new LinkedList<>(), instance.getClass());

        for (Field field : allFields) {
            FlowKey flowKey = field.getAnnotation(FlowKey.class);
            Argument ann = field.getAnnotation(Argument.class);

            if (flowKey != null) {
                // initialize field
                try { // ensure templated arguments
                    initField(instance, field, flowKey, ann, spec, initialArguments, globalConfigurations);
                }
                catch (Exception e) {
                    throw new ValidationException(e, "Could not initialize field " + field.getName()+": "+ e.getMessage() );
                }
            }

        }
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
    private static void initField(
            Object instance,
            Field field,
            FlowKey flowKey, Argument ann,
            Map<String, ?> spec,
            Map<String, Object> args,
            Map<String, Map<String, Object>> globalConfigurations
    )
            throws ValidationException, IllegalAccessException {
        // enable reflective access
        field.setAccessible(true);

        // this is the value which will get assigned to the field after evaluation
        Object value;
        Map<String, Object> allFields = null;
        Object jsonValue = spec.get(field.getName());
        Object globalValue = null;

        // TODO why is second condition always true?
        if(globalConfigurations != null) {
            String nodeName = instance.getClass().getSimpleName();

            //check if regex matches, and apply if valid
            for (String maybeRegex : globalConfigurations.keySet()) {
                if (maybeRegex.startsWith("/") && maybeRegex.endsWith("/")) {
                    String regex = maybeRegex.substring(1, maybeRegex.length() - 1);

                    boolean result = Pattern.compile(regex).matcher(nodeName).results()
                            .findAny().isPresent();

                    if (result) allFields = globalConfigurations.get(maybeRegex);

                    // fetch global value, if any
                    if (allFields != null) {
                        Object globalKey = allFields.get(field.getName());
                        if (globalKey != null) {
                            globalValue = globalKey;
                        }
                    }
                }
            }

            allFields = globalConfigurations.get(nodeName);

            // fetch global value, if any
            if (allFields != null) {
                Object globalKey = allFields.get(field.getName());
                if (globalKey != null) {
                    globalValue = globalKey;
                }
            }
        }

        value = NodeUtil.getValueForField(
                instance, field, jsonValue, globalValue,
                flowKey.mandatory(), flowKey.defaultValue(), flowKey.output(),
                ann != null, (ann != null ? ann.converter() : null),
                args
        );

        if(value != null) {
            field.set(instance, value);
        }
    }

    public static Object getValueForField(final Object instance,
                                          final Field field,
                                          final Object jsonValue,
                                          final Object globalValue,
                                          final boolean mandatory,
                                          final String defaultAnnotationValue,
                                          final boolean isOutput,
                                          final boolean isArgument,
                                          final Class<?> argumentConverter,
                                          final Map<String, Object> arguments
    ) throws ValidationException {
        try {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            Object fieldValue = field.get(instance);

            Object value;

            // use global value as least precedence
            value = globalValue;

            // prefer local JSON value over global parsed value
            if(jsonValue != null) value = jsonValue;

            // value has to be defined if mandatory
            if (mandatory && value == null) throw new ValidationException("Value has to be defined if mandatory: " + fieldType);

            // --------- current state of value
            // value != null: from JSON file
            // value == null: no JSON file definition

            // read optional JSON raw value if optional
            if(!mandatory && value == null) {
                Object converted;
                // TODO document what this code block does
                if(T.class.isAssignableFrom(fieldType)) {
                    converted = mapper.readValue(defaultAnnotationValue, Object.class);
                    value = converted;
                } else if (Enum.class.isAssignableFrom(fieldType)) {
                    converted = mapper.readValue(defaultAnnotationValue, String.class);
                    value = converted;
                } else {
                    converted = mapper.readValue(defaultAnnotationValue, fieldType);
                    value = converted;
                }
            }

            // argument annotation :
            //     String -> goTo primitive type or enum (default converter)
            //     String -> custom goTo                 (custom converter)
            if (isArgument && value != null) {

                // get template converter
                Method convert;
                if(argumentConverter == null || argumentConverter.equals(void.class))
                    convert = getConverter(StringToClassConverter.class);
                else
                    convert = getConverter(argumentConverter);

                // --- JSON string or some parsed value
                // if parsed value is a String (or value is JSON) replace arguments
                if (String.class.isAssignableFrom(value.getClass())) {
                    value = NodeUtil.replaceArguments(value, arguments);

                    // if replaced value is null, treat as disabled argument and set field value to null
                    if(((String) value).isEmpty()) {
                        return null;
                    }
                } // replace directly with the parsed value
                else {
                    // check if parsed value is an Integer but expected value is a Long
                    if(Integer.class.isAssignableFrom(value.getClass()) && Long.class.isAssignableFrom(fieldType)) {
                        // Integer passes as a Long
                        return ((Integer) value).longValue();
                    } else {
                        // else use parsed value
                        return value;
                    }
                }

                // --- String
                // convert the string value to the defined field type
                return invokeConverter(value, fieldType, convert);
            }

            // --------
            // value == null: no JSON definition, no optional definition: field null
            // value != null: parsed json object or field default (Template, MapKey)
            if(value == null) return null;

            // check if (input) template
            if (T.class.isAssignableFrom(fieldType) && !isOutput) {
                T<?> template = (T<?>) fieldValue;
                template.setParsedJson(convert(TypeToken.of(template.get()), value));
                return null;
            } // check if (output) template
            else if (T.class.isAssignableFrom(fieldType) && isOutput) {
                T<?> template = (T<?>) fieldValue;
                template.setParsedJson(convert(new TypeToken<String>(){}, value)); // targets only raw String type for now, no evaluation
                return null;
            }
            // if enum: try convert
            else if (Enum.class.isAssignableFrom(fieldType)) {
                value = Enum.valueOf(fieldType.asSubclass(Enum.class), String.valueOf(value));
            } // type match
                if (fieldType.isAssignableFrom(value.getClass())) {
                    // value is 'correct' only if no generics are used
                    if(fieldType.getTypeParameters().length>0)
                        throw new IllegalStateException("Generics are not supported by Java at runtime. " +
                                "Use T<> wrapper for field '" + field.getName()+"' of " + instance.getClass());
            } // check if field type is a general Address
            else if (String.class.isAssignableFrom(value.getClass()) && Address.class.isAssignableFrom(fieldType)) {
                value = NodeUtil.addressOf((String) value);
            } // try converting as a last resort
            else { // TODO #23 test this branch for full coverage
                value = StringToClassConverter.convert(String.valueOf(value), fieldType);
            }

//        log(TRACE,"Field '{}' initialized: {} ({})", field.getName(), field.get(this), (field.get(this) == null?"nullp":field.get(this).getClass()));

            return value;
        } catch (Exception e) {
            throw new ValidationException(e,  e.getMessage());
        }
    }

    public static Method getConverter(final Class<?> converter) throws ValidationException {
        Method convert;
        try { convert = converter.getMethod("convert", Object.class, Class.class); }
        catch (NoSuchMethodException e) { throw new ValidationException("Unknown template converter: " + e); }
        return convert;
    }

    public static Object invokeConverter(Object value, Class<?> fieldType, Method convert) throws ReflectiveOperationException {
        try {
            return convert.invoke(null, String.valueOf(value), fieldType);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ReflectiveOperationException(e);
        }
    }

    public static Object convert(T<?> token, Object value) throws ValidationException {
        TypeToken<?> templ = TypeToken.of(token.get());
        return convert(templ, value);
    }
    public static Object convert(TypeToken<?> templ, Object value) throws ValidationException {

        //null: return null
        if(value == null) return null;

        //check JSON object types
        //JSON list
        if(List.class.isAssignableFrom(value.getClass()) &&
                (
                        List.class.isAssignableFrom(templ.getRawType()) || templ.getRawType().equals(Object.class)
                )) {
            // create a list with converted values
            TypeToken<?> elementType = (templ.getRawType().equals(Object.class) ? templ : templ.resolveType(List.class.getTypeParameters()[0]));
            List<?> valueList = (List<?>) value;
//            log(TRACE,"Converting list: {}, expecting {}", valueList, elementType);

            List<Object> resultList = new ArrayList<>();

            for (Object o : valueList) {
                resultList.add(convert(elementType, o));
            }

            return resultList;
        } // JSON map
        else if(Map.class.isAssignableFrom(value.getClass()) &&
                (
                        Map.class.isAssignableFrom(templ.getRawType()) || templ.getRawType().equals(Object.class) // descend into object
                )) {
            // create a map with checked values
            TypeToken<?> elementType = (templ.getRawType().equals(Object.class) ? templ : templ.resolveType(Map.class.getTypeParameters()[1]));
            Map<?, ?> valueMap = (Map<?, ?>) value;
//            log(TRACE,"Converting map: {}, expecting value type {}", valueMap, elementType);

            Map<Object, Object> resultMap = new LinkedHashMap<>();
            for (Object key : valueMap.keySet()) {
                resultMap.put(key, convert(elementType, valueMap.get(key)));
            }

            return resultMap;
        } // JSON primitive
        else {
            // raw type
            if(templ.getRawType().isAssignableFrom(value.getClass()) && !String.class.isAssignableFrom(value.getClass())) {
                // same types, return actual object
                return value;
            } else if (String.class.isAssignableFrom(value.getClass())) {
                // string template found
                return TemplateUtil.parseTemplate(((String) value), templ);
            } else {
                throw new ValidationException("Argument type mismatch! Expected String or "+templ+", but found "+ value.getClass());
            }
        }
    }


    public static NodeAddress getNextNode(Address origin, Address goTo, Map<GraphAddress, List<NodeContainer<? extends Node>>> graphs) {
        throw new IllegalStateException();
    }

    public static Address getForwardTarget(Address origin, Map<GraphAddress, List<NodeContainer<? extends Node>>> graphs) {
//        for (GraphAddress k : graphs.keySet()) {
//            Iterator<NodeContainer<? extends Node>> it = graphs.get(k).iterator();
//            while(it.hasNext()) {
//                NodeContainer node = it.next();
//                if(node.getAddress().equalsTo(origin)){
//                    if(it.hasNext()) {
//                        return it.next().getAddress();
//                    } else {
//                        return null;
//                    }
//                }
//            }
//        }

        throw new IllegalStateException("Origin node address not found in any graph: " + origin.getRepresentation());
    }

    public static Optional<NodeContainer<? extends Node>> getNode(Address target, Map<GraphAddress, List<NodeContainer<? extends Node>>> graphs, Map<InstanceAddress, ScrapeInstance> importedInstances) {
//        for (InstanceAddress instanceAddress : importedInstances.keySet()) {
//            if (target.equalsTo(instanceAddress)) {
//                return importedInstances.get(instanceAddress).getEntryGraph().get(0);
//            }
//
//            // can only resolve if instance address is correct
//            Address insideTarget = target.resolve(instanceAddress);
//            if(insideTarget != null) return importedInstances.get(instanceAddress).getNode(insideTarget);
//        }
//
//        for (GraphAddress k : graphs.keySet()) {
//            if(k.equalsTo(target)) {
//                return graphs.get(k).get(0);
//            }
//
//            for (NodeContainer node : graphs.get(k)) {
//                if(node.getAddress().equalsTo(target))
//                    return node;
//            }
//        }

        throw new IllegalArgumentException("Node address not existing! "+target);
    }

    public static Map<String, String> extractMapFromFields(List<Field> outputData, NodeContainer target) {
        Map<String, String> outputResult = new HashMap<>();
        for (Field output : outputData) {
            String name = output.getName();
            output.setAccessible(true);
            try {
                String value = ((T) output.get(target)).get().getTypeName();

                outputResult.put(name, value);
            } catch (IllegalAccessException e) {
                // TODO handle exception differently
                throw new RuntimeException(e);
            }
        }

        return outputResult;
    }

    public static NodeContainer<? extends Node> getTarget(NodeAddress origin, Address goTo, ScrapeInstance jobInstance) {
        try {
            Optional<NodeContainer<? extends Node>> localTarget;
            if (goTo.isAbsolute()) {
                return jobInstance.getNode(goTo).get();
            } else if(goTo.isRelative()) {
                // local graph node, not self
                Address local = origin.replace(goTo.getRepresentation());
                localTarget = jobInstance.getNode(local);
                if(localTarget.isPresent()) return localTarget.get();

                // graph relative
                Address graph = new GraphAddressImpl(jobInstance.getName(), goTo.getRepresentation());
                localTarget = jobInstance.getNode(graph);
                if(localTarget.isPresent()) return localTarget.get();

                // imported instance
                ScrapeInstance importedInstance = jobInstance.getImportedInstances().get(new InstanceAddressImpl(goTo.getRepresentation()));
                if(importedInstance == null)
                    throw new IllegalStateException(origin+": Address is neither in local graph, relative graph, or imported instance: "+goTo);
                return importedInstance.getEntry();
            } else {
                // graph relative
                Address graph = addressOf(
                        jobInstance.getName()+"."+
                                goTo.getRepresentation().split("\\.")[0]+"."+
                                goTo.getRepresentation().split("\\.")[1]
                );

                localTarget = jobInstance.getNode(graph);
                if(localTarget.isPresent()) return localTarget.get();

                // imported instance
                String instanceTarget = goTo.getRepresentation().split("\\.")[0];

                ScrapeInstance importedInstance = jobInstance.getImportedInstances().get(
                        new InstanceAddressImpl(instanceTarget)
                );
                if(importedInstance == null)
                    throw new IllegalStateException(origin+": Neither graph relative address nor imported instance found for " + goTo);
                Optional<NodeContainer<? extends Node>> imported = importedInstance.getNode(addressOf(goTo.getRepresentation() + ".0"));
                return imported.get();
            }
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Address "+goTo+" not found");
        }
    }
}
