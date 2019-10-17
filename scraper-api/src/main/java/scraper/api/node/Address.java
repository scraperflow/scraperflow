package scraper.api.node;

// currently only used for comparisons (compareTo)
public interface Address {
    /** Unique string representation */
    String getRepresentation();

    default boolean equalsTo(Address o) {
        return getRepresentation().equals(o.getRepresentation());
    }
}
