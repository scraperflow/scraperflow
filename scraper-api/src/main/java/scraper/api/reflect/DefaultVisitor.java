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
    public void visitMapOrListLookup(MapOrListLookup<?> mapOrListLookup) {
        try {
            mapOrListLookup.getListObjectTerm().accept(this);
            mapOrListLookup.getMapObjectTerm().accept(this);
        } catch (Exception e) {
            mapOrListLookup.getIndexTerm().accept(this);
            mapOrListLookup.getKeyTerm().accept(this);
        }
    }
}
