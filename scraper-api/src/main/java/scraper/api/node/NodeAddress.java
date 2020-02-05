package scraper.api.node;

import scraper.annotations.NotNull;

public interface NodeAddress extends Address {
    @NotNull Address nextIndex();
    @NotNull Address replace(@NotNull String representation);
}
