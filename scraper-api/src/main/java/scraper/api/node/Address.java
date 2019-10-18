package scraper.api.node;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;

public interface Address {
    /** Unique string representation */
    @NotNull String getRepresentation();

    /** Check if this address denotes the same target as the given address */
    default boolean equalsTo(@NotNull Address o) { return getRepresentation().equals(o.getRepresentation()); }

    /**
     * Resolves this address with the one given, e.g.
     *
     * [mod.targetNode].resolve([mod]) -> [targetNode]
     */
    @Nullable Address resolve(@NotNull Address toResolve);
}
