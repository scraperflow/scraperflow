package scraper.api.node;

import scraper.annotations.NotNull;

import java.util.Optional;

public interface NodeAddress extends Address {
    @NotNull Address nextIndex();
    @NotNull Address replace(@NotNull String representation);

    @NotNull String getNode();
    @NotNull String getGraph();
    @NotNull String getInstance();
    @NotNull Integer getIndex();
    @NotNull Optional<String> getLabel();

    @NotNull Address getOnlyIndex();
    @NotNull Optional<Address> getOnlyLabel();
}
