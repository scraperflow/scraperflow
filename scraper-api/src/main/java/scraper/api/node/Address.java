package scraper.api.node;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;

// currently only used for comparisons (compareTo)
public interface Address {
    /** Unique string representation */
    @NotNull String getRepresentation();
    default boolean equalsTo(@NotNull Address o) { return getRepresentation().equals(o.getRepresentation()); }

    @Nullable Address resolve(@NotNull Address toResolve);
}
