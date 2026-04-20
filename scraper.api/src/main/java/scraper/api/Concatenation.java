package scraper.api;

import scraper.annotations.NotNull;

import java.util.List;

public interface Concatenation extends Term<String> {
    @NotNull List<Term<String>> getConcatenationTerms();
}
