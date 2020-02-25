package scraper.api.reflect;

public class DefaultVisitor implements TVisitor {
    @Override
    public void visitFlowKeyLookup(FlowKeyLookup<?> mapKey) { mapKey.getKeyLookup().accept(this); }

    @Override
    public void visitPrimitive(Primitive<?> primitive) {}

    @Override
    public void visitConcatenation(Concatenation<?> concat) {
        concat.getConcatTemplatesOrStrings().forEach(t -> t.accept(this));
    }

    @Override
    public void visitListTerm(ListTerm<?> list) {
        list.getTerms().forEach(t -> t.accept(this));
    }

    @Override
    public void visitMapTerm(MapTerm<?> mapTerm) {
        mapTerm.getTerms().forEach((k,t) -> t.accept(this));
    }

    @Override
    public void visitListLookup(ListLookup<?> listLookup) {
        listLookup.getListObjectTerm().accept(this);
        listLookup.getIndexTerm().accept(this);
    }

    @Override
    public void visitMapLookup(MapLookup<?> mapLookup) {
        mapLookup.getMapObjectTerm().accept(this);
        mapLookup.getKeyTerm().accept(this);
    }
}
