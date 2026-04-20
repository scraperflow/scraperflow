package scraper.plugins.core.typechecker.visitors;

import scraper.api.*;

public class EnsureConstant extends DefaultVisitor<Void> {

    @Override
    public Void visitFlowKeyLookup(FlowKeyLookup<?> mapKey) {
        throw new TemplateException("Not a constant");
    }

    @Override
    public Void visitPrimitive(Primitive<?> primitive) {
        return super.visitPrimitive(primitive);
    }

    @Override
    public Void visitConcatenation(Concatenation concat) {
        throw new TemplateException("Not a constant");
    }

    @Override
    public Void visitListTerm(ListTerm<?> list) {
        return super.visitListTerm(list);
    }

    @Override
    public Void visitMapTerm(MapTerm<?> mapTerm) {
        return super.visitMapTerm(mapTerm);
    }

    @Override
    public Void visitListLookup(ListLookup<?> listLookup) {
        throw new TemplateException("Not a constant");
    }

    @Override
    public Void visitMapLookup(MapLookup<?> mapLookup) {
        throw new TemplateException("Not a constant");
    }
}
