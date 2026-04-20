package scraper.api;

import scraper.annotations.NotNull;

import java.util.List;

public interface ListLookup<Y> extends Term<Y> {
    @NotNull Term<List<Y>> getListObjectTerm();
    @NotNull Term<Integer> getIndexTerm();
    @NotNull String getTypeStringElement();
}
