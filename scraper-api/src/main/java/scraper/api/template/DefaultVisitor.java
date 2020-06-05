package scraper.api.template;

import scraper.annotations.NotNull;

public class DefaultVisitor<A> implements TVisitor<A> {
    @Override
    public A visitFlowKeyLookup(@NotNull FlowKeyLookup<?> mapKey) {
        mapKey.getKeyLookup().accept(this);
        return null;
    }

    @Override
    public A visitPrimitive(@NotNull Primitive<?> primitive) { return null; }

    @Override
    public A visitConcatenation(@NotNull Concatenation concat) {
        concat.getConcatenationTerms().forEach(t -> t.accept(this));
        return null;
    }

    @Override
    public A visitListTerm(@NotNull ListTerm<?> list) {
        list.getTerms().forEach(t -> t.accept(this));
        return null;
    }

    @Override
    public A visitMapTerm(@NotNull MapTerm<?> mapTerm) {
        mapTerm.getTerms().forEach((k,t) -> t.accept(this));
        return null;
    }

    @Override
    public A visitListLookup(@NotNull ListLookup<?> listLookup) {
        listLookup.getListObjectTerm().accept(this);
        listLookup.getIndexTerm().accept(this);
        return null;
    }

    @Override
    public A visitMapLookup(@NotNull MapLookup<?> mapLookup) {
        mapLookup.getMapObjectTerm().accept(this);
        mapLookup.getKeyTerm().accept(this);
        return null;
    }
}
