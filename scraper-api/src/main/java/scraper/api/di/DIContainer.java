package scraper.api.di;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;

/**
 * Minimal dependency injection container. It can handle single beans and beans in a collection.
 *
 * @since 1.0.0
 */
public interface DIContainer {
    /**
     * Invokes the {@link DIContainer#addComponent(Class, boolean)} with the multipleComponentsAllowed flag set to false.
     * @see DIContainer#addComponent(Class, boolean)
     */
    void addComponent(@NotNull Class<?> targetClass) throws IllegalArgumentException;

    /**
     * Adds a target class to the container.
     * Resolves the dependencies.
     * If dependencies are missing, waits for other components to be added to try to resolve
     * the dependencies again.
     * If target interface is to be added multiple times to the container to be used as a collection
     * dependency, the multipleComponentsAllowed flag has to be set.
     *
     * @param targetClass Bean to be added to the container
     * @throws IllegalArgumentException if DIContainer is not used as intended
     */
    void addComponent(@NotNull Class<?> targetClass, boolean multipleComponentsAllowed);

    /**
     * Try to get an instantiated version of the target class.
     *
     * @param targetClass target class to get an instance
     * @return instance, or null if not available or still waiting for unmet dependencies
     */
    @Nullable <T> T get(@NotNull Class<T> targetClass);
}
