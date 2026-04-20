package scraper.api;

import scraper.annotations.NotNull;

public enum NodeLogLevel {
    // do not change the order of these enums
    // the ordinal value is used to compare different log levels
    TRACE, DEBUG, INFO, WARN, ERROR;

    public boolean worseOrEqual(@NotNull NodeLogLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
