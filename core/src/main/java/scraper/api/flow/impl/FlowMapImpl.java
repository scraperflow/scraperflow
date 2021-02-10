package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.template.L;
import scraper.api.template.Primitive;
import scraper.api.template.T;
import scraper.core.IdentityEvaluator;
import scraper.util.TemplateUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.DEBUG;
import static scraper.util.TemplateUtil.listOf;
import static scraper.util.TemplateUtil.mapOf;

public class FlowMapImpl extends IdentityEvaluator implements FlowMap {

    public ConcurrentMap<String, Object> getPrivateMap() {
        return privateMap;
    }

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
        FlowMapImpl o = new FlowMapImpl();
        args.forEach((k,v) -> o.getPrivateMap().putAll(args));
        return o;
    }

    @NotNull
    public void remove(@NotNull String location) {
        privateMap.remove(location);
    }

    public void clear() {
        privateMap.clear();
    }

    @Override
    public @NotNull String toString() {
        return "[Map: "+privateMap.toString()+"]";
    }


    public @NotNull ConcurrentMap<String, Object> getMap() {
        return new ConcurrentHashMap<>(privateMap);
    }

    public boolean containsElements(@NotNull final FlowMap expectedOutput) {
        for (String key : ((FlowMapImpl) expectedOutput).privateMap.keySet()) {
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
        try {
            A eval = template.getTerm().eval(this);
            return Optional.ofNullable(eval);
        } catch (Exception e) {
            return Optional.empty();
        }
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
        if(object == null) {
            privateMap.remove(location);
        } else {
            output(location, object);
        }
    }

    @Override
    public void forEach(BiConsumer<L<?>, Object> consumer) {
        Map<L<?>, Object> newMap = privateMap.entrySet()
                .stream()
                .map(e -> Map.entry(TemplateUtil.locationOf(e.getKey()), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        newMap.forEach(consumer::accept);
    }

    public void output(@NotNull String location, @NotNull Object outputObject) {
        privateMap.put(location, outputObject);
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


    private @NotNull static final System.Logger log = System.getLogger("TypeInfer");

    // ======
    // Runtime
    // ======

    public static T<?> inferTypeToken(Object o) {
        return inferType(o);
    }


    @SuppressWarnings("rawtypes") // fully checking types and members of list and map
    public static T<?> inferType(Object o) {
        if(o instanceof List) {
            List oList = ((List) o);
            if(((List) o).isEmpty()) return new T<List>(){};

            // full check
            T<?> commonType = inferType(oList.get(0));
            for (int i = 1; i < oList.size(); i++) {
                T<?> nextType = inferType(oList.get(i));

                if(commonType.equalsType(nextType))
                    continue;

                log.log(DEBUG, "Element type of list was generalized: {0} U {1} -> Object", commonType, nextType);
                commonType = new T<>(){};
                break;
            }

            return listOf(commonType);
        } else if (o instanceof Map) {
            Map oMap = ((Map) o);
            if(((Map) o).isEmpty()) return new T<Map>(){};

            // full check
            Iterator iter = oMap.keySet().iterator();
            T<?> commonType = inferType(oMap.get(iter.next()));
            while(iter.hasNext()) {
                T<?> nextType = inferType(oMap.get(iter.next()));
                if(commonType.equalsType(nextType))
                    continue;

                log.log(DEBUG, "Element type of map was generalized: {0} U {1} -> Object", commonType, nextType);
                commonType = new T<>(){};
                break;
            }

            return mapOf(new T<String>(){}, commonType);

        } else {
            if(o == null) {
                return new T<>(){};
            } else {
                // raw type
                return new T<>(o.getClass()){};
            }
        }
    }

}
