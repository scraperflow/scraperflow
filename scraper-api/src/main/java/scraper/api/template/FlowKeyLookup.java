package scraper.api.template;

import scraper.annotations.NotNull;

public interface FlowKeyLookup<Y> extends Term<Y> {
    @NotNull Term<String> getKeyLookup();
    boolean isTypeVariable();
}
