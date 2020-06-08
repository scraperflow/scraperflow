package scraper.api.flow.impl;

import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.template.L;
import scraper.api.template.Primitive;
import scraper.api.template.T;
import scraper.core.IdentityEvaluator;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static scraper.core.converter.StringToClassConverter.convert;
import static scraper.util.TemplateUtil.listOf;
import static scraper.util.TemplateUtil.mapOf;

public class FlowMapImpl extends IdentityEvaluator implements FlowMap {

    private final ConcurrentMap<String, Object> privateMap;

    private UUID parentId;
    private Integer parentSequence;
    private int sequence = 0;
    private UUID uuid;

    public FlowMapImpl(
            @NotNull ConcurrentMap<String, Object> privateMap,
            UUID parentId,
            UUID uuid,
            Integer parentSequence,
            int sequence
    ) {
        this.privateMap = privateMap;
        this.parentId = parentId;
        this.parentSequence = parentSequence;
        this.uuid = uuid;
        this.sequence = sequence;
    }

    public FlowMapImpl(@NotNull UUID parentId, Integer parentSequence) {
        privateMap = new ConcurrentHashMap<>();
        this.parentId = parentId;
        this.parentSequence = parentSequence;
        uuid = UUID.randomUUID();
    }

    public FlowMapImpl() {
        privateMap = new ConcurrentHashMap<>();
        parentId = null;
        parentSequence = null;
        uuid = UUID.randomUUID();
    }


    public static FlowMap origin() {
        return new FlowMapImpl();
    }

    public static FlowMap origin(Map<String, Object> args) {
        FlowMap o = new FlowMapImpl();
        args.forEach(o::output);
        return o;
    }

    @NotNull @Override
    public void remove(@NotNull String location) {
        privateMap.remove(location);
    }

    @NotNull
    @Override
    public Optional<? super Object> get(@NotNull String expected) {
        return Optional.ofNullable(privateMap.get(expected));
    }


    @Override
    public int size() {
        return privateMap.size();
    }

    @Override
    public void clear() {
        privateMap.clear();
    }

    @Override
    public @NotNull Set<String> keySet() {
        return privateMap.keySet();
    }

    @Override
    public @NotNull String toString() {
        return "[Map: "+privateMap.toString()+"]";
    }


    public @NotNull ConcurrentMap<String, Object> getMap() {
        return new ConcurrentHashMap<>(privateMap);
    }

    public boolean containsElements(@NotNull final FlowMap expectedOutput) {
        for (String key : expectedOutput.keySet()) {
            Object thisElement = privateMap.get(key);
            Object thatElement = ((FlowMapImpl) expectedOutput).privateMap.get(key);
            if(thisElement != null && thatElement != null) {
                if(!compareElement(thisElement, thatElement)) return false;
            } else if(thisElement == null && thatElement != null) return false;
        }

        return true;
    }

    @NotNull
    @Override
    public Optional<UUID> getParentId() {
        return Optional.ofNullable(parentId);
    }

    @NotNull
    @Override
    public Optional<Integer> getParentSequence() {
        return Optional.ofNullable(parentSequence);
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    @Override
    public void nextSequence() {
        sequence++;
    }

    @NotNull
    @Override public UUID getId() { return uuid; }

    @NotNull @Override
    public <A> A eval(@NotNull T<A> template) {
        if(template.getTerm() == null)
            throw new TemplateException("Template was not set but not expected, " +
                    "wrong node implementation");
        A evaluated = template.getTerm().eval(this);
        if(evaluated == null)
            throw new TemplateException("Template was evaluated to null but not expected, " +
                    "either wrong node implementation/usage or type safety violated");
        return evaluated;
    }

    @NotNull
    @Override
    public <A> String evalLocation(@NotNull L<A> template) {
        if(template.getLocation().getClass().isAssignableFrom(Primitive.class))
            throw new TemplateException("A location template has to be a primitive: " + template.getLocation().getClass()); // for now
        String location = template.getLocation().eval(this);
        if(location == null)
            throw new TemplateException("Location could not be evaluated: " + template.getLocation().getRaw());
        return location;
    }


    @NotNull @Override
    public <A> Optional<A> evalMaybe(@NotNull T<A> template) {
        if(template.getTerm() == null) return Optional.empty();
        A eval = template.getTerm().eval(this);
        return Optional.ofNullable(eval);
    }

    @NotNull
    @Override
    public <A> Optional<String> evalLocationMaybe(@NotNull L<A> template) {
        return Optional.ofNullable(template.getLocation().eval(this));
    }


    @NotNull @Override
    public <A> A evalIdentity(@NotNull T<A> t) {
        if(t.getTerm() == null)
            throw new TemplateException("Template was set to null but not expected, " +
                    "wrong node implementation");
        A eval = t.getTerm().eval(new IdentityFlowMap());
        if(eval == null)
            throw new TemplateException("Template was evaluated to null but not expected, " +
                    "either wrong node implementation/usage or type safety violated");
        return eval;
    }

    @NotNull @Override
    public <A> Optional<A> evalIdentityMaybe(@NotNull T<A> template) {
        return Optional.ofNullable(evalIdentity(template));
    }

    @Override
    public <A> void output(@NotNull L<A> locationAndType, A object) {
        String location = evalLocation(locationAndType);
        output(location, object);
//        privateMap.put(location, object);
//        privateTypeMap.put(location, locationAndType);
    }

    @Override
    public void output(@NotNull String location, Object outputObject) {
        try {
            privateMap.put(location, outputObject);
        } catch (Exception e) {
            log.error("Could not infer type for key '{}' and actual object '{}': {}", location, outputObject, e.getMessage());
            throw new TemplateException(e, "Could not infer type for key '"+location+"' and actual object '"+outputObject+"': "+ e.getMessage());
        }
    }


    @NotNull
    @Override
    public FlowMap copy() {
        return copy(this);
    }

    @NotNull
    @Override
    public FlowMap newFlow() {
        return new FlowMapImpl(privateMap, uuid, UUID.randomUUID(), sequence, 0);
    }

    private boolean descendMap(@NotNull final Map<?,?> currentMap, @NotNull final Map<?,?> otherMap) {
        for (Object s : otherMap.keySet()) {
            Object otherElement = otherMap.get(s);
            if(otherElement == null) return false;

            Object thisElement = currentMap.get(s);

            if(!compareElement(thisElement, otherElement)) {
                return false;
            }
        }

        return true;
    }

    private boolean compareElement(@Nullable final Object thisElement, @Nullable final Object otherElement) {
        if(thisElement == null || otherElement == null) return false;

        if(Map.class.isAssignableFrom(thisElement.getClass()) && Map.class.isAssignableFrom(otherElement.getClass())) {
            return descendMap((Map<?, ?>) thisElement, (Map<?, ?>) otherElement);
        } else
        if(Collection.class.isAssignableFrom(thisElement.getClass()) && Collection.class.isAssignableFrom(otherElement.getClass())) {
            return descendCollection((Collection<?>) thisElement, (Collection<?>) otherElement);
        } else {
            return thisElement.equals(otherElement);
        }

    }

    private boolean descendCollection(@NotNull final Collection<?> currentCollection, @NotNull final Collection<?> otherCollection) {
        // if empty, current collection should also be empty
        if(otherCollection.isEmpty() && !currentCollection.isEmpty()) return false;

        for (Object otherObject : otherCollection) {
            boolean foundAtLeastOneMatch = false;

            for (Object currentObject : currentCollection) {
                foundAtLeastOneMatch = compareElement(currentObject, otherObject);
                if(foundAtLeastOneMatch) break;
            }

            if (!foundAtLeastOneMatch) return false;

        }

        return true;
    }

//    public static synchronized @NotNull FlowMapImpl of(final @NotNull Map<String, Object> initialArguments) {
//        return new FlowMapImpl(new ConcurrentHashMap<>(Objects.requireNonNullElseGet(initialArguments, Map::of)));
//    }

    public static synchronized @NotNull FlowMapImpl copy(final @NotNull FlowMap o) {
        return new FlowMapImpl(
                new ConcurrentHashMap<>(((FlowMapImpl) o).privateMap),
                ((FlowMapImpl) o).parentId,
                ((FlowMapImpl) o).uuid,
                ((FlowMapImpl) o).parentSequence,
                ((FlowMapImpl) o).sequence
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowMapImpl flowMap = (FlowMapImpl) o;
        return privateMap.equals(flowMap.privateMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateMap);
    }


    private @NotNull static final Logger log = org.slf4j.LoggerFactory.getLogger("TypeInfer");

    // ======
    // Runtime type checking
    // ======

    @NotNull
    @Override
    public <K> Optional<K> getWithType(@NotNull String targetKey, @NotNull Type targetType) {
        Object targetObject = privateMap.get(targetKey);
        if(targetObject == null) return Optional.empty();

//        // for each inserted element there should be an associated type token
//        assert privateTypeMap.containsKey(targetKey);
//        T<?> knownType = privateTypeMap.get(targetKey);

//        TypeToken<?> knownToken = TypeToken.of(knownType.get());
//        TypeToken<?> targetToken = TypeToken.of(targetType.get());
//
//        if(targetToken.isSupertypeOf(knownToken)) {
//            @SuppressWarnings("unchecked") // checked with subtype relation
//                    K targetO = (K) targetObject;
//            return Optional.of(targetO);
//        }
//
//        if(targetToken.isSubtypeOf(knownToken)) {
//            log.warn("Downcasting '{}' from '{}' -> '{}' ('{}'). This may indicate a node implementation error or an insufficient type infer of that key",
//                    targetKey, knownType.get(), targetType.get(), targetType);
//            @SuppressWarnings("unchecked") // warned user about potentially bad downcast
//                    Optional<K> castObject = Optional.of((K) targetObject);
//            return castObject;
//        }


        try {
            K converted = (K) convert(targetObject, TypeToken.of(targetType).getRawType());
            Optional<K> castObject = Optional.of((K) converted);
            return castObject;
        }
        catch (Exception e){
            log.error("Could not check type", e);
            throw new TemplateException("Runtime template error");
        }
    }

    public static Map<String, T<?>> checkGenericTypeAndCapture(Map<String, T<?>> captures, Type known, T<?> target) {
        return new GenericTypeMatcher() {}.visitAndReturnCaptures(captures, known, target.get());
    }

    public static Map<String, T<?>> checkGenericTypeAndCapture(T<?> known, T<?> target) {
        return new GenericTypeMatcher() {}.visitAndReturnCaptures(known.get(), target.get());
    }



    public static T<?> inferTypeToken(Object o) {
        TypeToken<?> token = inferType(o);
        return new T<>(token.getType()){};
    }


    @SuppressWarnings("rawtypes") // fully checking types and members of list and map
    public static TypeToken<?> inferType(Object o) {
        if(o instanceof List) {
            List oList = ((List) o);
            if(((List) o).isEmpty()) return TypeToken.of(List.class);

            // full check
            TypeToken<?> commonType = inferType(oList.get(0));
            for (int i = 1; i < oList.size(); i++) {
                TypeToken<?> nextType = inferType(oList.get(i));

                if(commonType.equals(nextType))
                    continue;

                log.debug("Element type of list was generalized: {} U {} -> Object", commonType, nextType);
                commonType = TypeToken.of(Object.class);
                break;
            }

            return listOf(commonType);
        } else if (o instanceof Map) {
            Map oMap = ((Map) o);
            if(((Map) o).isEmpty()) return TypeToken.of(Map.class);

            // full check
            Iterator iter = oMap.keySet().iterator();
            TypeToken<?> commonType = inferType(oMap.get(iter.next()));
            while(iter.hasNext()) {
                TypeToken<?> nextType = inferType(oMap.get(iter.next()));
                if(commonType.equals(nextType))
                    continue;

                log.debug("Element type of map was generalized: {} U {} -> Object", commonType, nextType);
                commonType = TypeToken.of(Object.class);
                break;
            }

            return mapOf(TypeToken.of(String.class), commonType);

        } else {
            // raw type
            return TypeToken.of(o.getClass());
        }
    }

}
