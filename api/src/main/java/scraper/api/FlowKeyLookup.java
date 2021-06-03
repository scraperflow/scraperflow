package scraper.api;

import scraper.annotations.NotNull;

public interface FlowKeyLookup<Y> extends Term<Y> {
    @NotNull Term<String> getKeyLookup();
    boolean isTypeVariable();
}
