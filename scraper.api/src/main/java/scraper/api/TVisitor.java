package scraper.api;

import scraper.annotations.NotNull;

public interface TVisitor<A> {
    A visitFlowKeyLookup(@NotNull FlowKeyLookup<?> mapKey);
    A visitPrimitive(@NotNull Primitive<?> primitive);
    A visitConcatenation(@NotNull Concatenation concat);
    A visitListTerm(@NotNull ListTerm<?> list);
    A visitMapTerm(@NotNull MapTerm<?> mapTerm);
    A visitListLookup(@NotNull ListLookup<?> listLookup);
    A visitMapLookup(@NotNull MapLookup<?> mapLookup);
}
