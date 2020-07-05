package scraper.api.di.impl;


import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.annotations.di.DITarget;
import scraper.api.di.DIContainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.Logger.Level.*;

// if something goes wrong, then at the beginning at the container build up
// so this is fine, I think
@SuppressWarnings("unchecked")
public class DIContainerImpl implements DIContainer {

    private static final System.Logger log = System.getLogger("DependencyInjector");

    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Object>> multiInstances = new ConcurrentHashMap<>();

    private final List<Runnable> queue = new LinkedList<>();

    @Override
    public void addComponent(@NotNull final Class<?> targetClass) { addComponent(targetClass, false); }

    @Override
    public void addComponent(@NotNull final Class<?> targetClass, final boolean multipleAllowed) {
        if(targetClass.isInterface()) throw new IllegalArgumentException("Only actual implementations allowed: " + targetClass.getName());
        if(targetClass.getDeclaredConstructors().length != 1) throw new IllegalArgumentException("Expecting only 1 declared constructor: "+targetClass.getName());

        Constructor<?> constructor = targetClass.getDeclaredConstructors()[0];

        log.log(TRACE,"Adding implementation: {0}", targetClass.getSimpleName());

        Object[] initArgs = getInitArgs(constructor);
        if(initArgs == null) {
            log.log(TRACE,"Missing dependencies for {0}. Waiting until more components are added", targetClass.getName());
            Runnable later = () -> addComponent(targetClass, multipleAllowed);
            queue.add(later);
            return;
        }

        try {
            // populate with instance
            if(multipleAllowed || (instances.get(targetClass) == null)) {
                Object newInstance = constructor.newInstance(initArgs);
                if(multipleAllowed) {
                    List<Object> multi = multiInstances.getOrDefault(targetClass, new ArrayList<>());
                    multi.add(newInstance);
                    multiInstances.put(targetClass, multi);
                    for (Class<?> implementedInterfaces : targetClass.getInterfaces()) {
                        List<Object> multiI = multiInstances.getOrDefault(implementedInterfaces, new ArrayList<>());
                        multiI.add(newInstance);
                        multiInstances.put(implementedInterfaces, multiI);
                    }
                } else {
                    instances.put(targetClass, newInstance);
                    for (Class<?> implementedInterfaces : targetClass.getInterfaces()) {
                        instances.put(implementedInterfaces, newInstance);
                    }
                }
            } else {
                throw new IllegalArgumentException("Already added instance of " + targetClass.getName());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Could not created new instance of " + targetClass.getName(), e);
        }

        LinkedList<Runnable> runnables = new LinkedList<>(queue);
        for (Runnable runnable : runnables) {
            queue.remove(runnable);
            runnable.run();
        }
    }

    @Override
    public <T> T get(@NotNull Class<T> targetClass) {
        for (Class<?> a : multiInstances.keySet()) {
            for (Object knownInstance : multiInstances.get(a)) {
                if(knownInstance.getClass().getName().equalsIgnoreCase(targetClass.getName()))
                    return (T) knownInstance;
            }
        }

        return (T) instances.get(targetClass);
    }

    @Override
    public <T> Collection<T> getCollection(Class<T> targetClass) {
        for (Class<?> a : multiInstances.keySet()) {
            if(a.equals(targetClass)) return (Collection<T>) multiInstances.get(a);
        }

        return Set.of();
    }


    private @Nullable Object[] getInitArgs(@NotNull final Constructor<?> constructor) {
        if(constructor.getParameterCount() == 0) return new Object[0];

        List<Object> dependencies = new ArrayList<>();
        Annotation[][] annotations = constructor.getParameterAnnotations();
        int index = 0;

        for (Class<?> dependency : constructor.getParameterTypes()) {
            if(dependency.isAssignableFrom(Collection.class)) {
                if(annotations[index].length != 1) throw new IllegalArgumentException("Expected exactly one annotation with goTo type of collection");

                DITarget targetAnnotation = (DITarget) annotations[index][0];
                Collection<?> dependencyCollection = multiInstances.get(targetAnnotation.value());
                if(dependencyCollection == null) dependencyCollection = Set.of();
                dependencies.add(dependencyCollection);
            } else {
                Object dependencyInstance = instances.get(dependency);
                if(dependencyInstance == null) return null;
                dependencies.add(dependencyInstance);
            }

            index++;
        }

        return dependencies.toArray();
    }
}
