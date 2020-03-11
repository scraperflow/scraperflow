package scraper.api.template;

import scraper.annotations.NotNull;

import java.util.List;

public interface ListLookup<Y> extends Term<Y> {
    @NotNull Term<List> getListObjectTerm();
    @NotNull Term<Integer> getIndexTerm();
}
