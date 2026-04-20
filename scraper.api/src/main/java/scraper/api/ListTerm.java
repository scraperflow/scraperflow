package scraper.api;

import scraper.annotations.NotNull;

import java.util.List;

public interface ListTerm<Y> extends Term<List<Y>> {

    T<Y> getElementType();
    @NotNull List<Term<Y>> getTerms();
    boolean isTypeVariable();
}
