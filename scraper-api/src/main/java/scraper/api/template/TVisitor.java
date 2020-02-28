package scraper.api.template;

import scraper.annotations.NotNull;

public interface TVisitor {
    void visitFlowKeyLookup(@NotNull FlowKeyLookup<?> mapKey);
    void visitPrimitive(@NotNull Primitive<?> primitive);
    void visitConcatenation(@NotNull Concatenation concat);
    void visitListTerm(@NotNull ListTerm<?> list);
    void visitMapTerm(@NotNull MapTerm<?> mapTerm);
    void visitListLookup(@NotNull ListLookup<?> listLookup);
    void visitMapLookup(@NotNull MapLookup<?> mapLookup);
}
