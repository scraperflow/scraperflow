package scraper.api.template;

import scraper.annotations.NotNull;

public class DefaultVisitor implements TVisitor {
    @Override
    public void visitFlowKeyLookup(@NotNull FlowKeyLookup<?> mapKey) { mapKey.getKeyLookup().accept(this); }

    @Override
    public void visitPrimitive(@NotNull Primitive<?> primitive) {}

    @Override
    public void visitConcatenation(@NotNull Concatenation concat) {
        concat.getConcatenationTerms().forEach(t -> t.accept(this));
    }

    @Override
    public void visitListTerm(@NotNull ListTerm<?> list) {
        list.getTerms().forEach(t -> t.accept(this));
    }

    @Override
    public void visitMapTerm(@NotNull MapTerm<?> mapTerm) {
        mapTerm.getTerms().forEach((k,t) -> t.accept(this));
    }

    @Override
    public void visitListLookup(@NotNull ListLookup<?> listLookup) {
        listLookup.getListObjectTerm().accept(this);
        listLookup.getIndexTerm().accept(this);
    }

    @Override
    public void visitMapLookup(@NotNull MapLookup<?> mapLookup) {
        mapLookup.getMapObjectTerm().accept(this);
        mapLookup.getKeyTerm().accept(this);
    }
}
