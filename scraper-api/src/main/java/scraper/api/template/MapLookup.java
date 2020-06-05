package scraper.api.template;

import scraper.annotations.NotNull;

import java.util.Map;

public interface MapLookup<Y> extends Term<Y> {
    @NotNull Term<Map<String, Y>> getMapObjectTerm();
    @NotNull Term<String> getKeyTerm();
}
