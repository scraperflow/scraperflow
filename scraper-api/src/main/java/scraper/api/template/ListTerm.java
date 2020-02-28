package scraper.api.template;

import scraper.annotations.NotNull;

import java.util.List;

public interface ListTerm<Y> extends Term<List<Y>> {
    @NotNull List<Term<Y>> getTerms();
}
