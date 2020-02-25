package scraper.api.reflect;

public interface TVisitor {
    void visitFlowKeyLookup(FlowKeyLookup<?> mapKey);
    void visitPrimitive(Primitive<?> primitive);
    void visitConcatenation(Concatenation<?> concat);
    void visitListTerm(ListTerm<?> list);
    void visitMapTerm(MapTerm<?> mapTerm);
//    void visitMapOrListLookup(MapOrListLookup<?> mapOrListLookup);
    void visitListLookup(ListLookup<?> listLookup);
    void visitMapLookup(MapLookup<?> mapLookup);
}
