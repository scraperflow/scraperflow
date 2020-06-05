package scraper.plugins.core.typechecker.visitors;

import scraper.api.exceptions.TemplateException;
import scraper.api.template.*;

import java.lang.reflect.Type;
import java.util.Map;

public class ReplaceCaptures extends DefaultVisitor<Term<?>> {

    private final Map<String, T<?>> captures;

    public ReplaceCaptures(Map<String, T<?>> captures) {
        this.captures = captures;
    }

    @Override
    public Term<?> visitFlowKeyLookup(FlowKeyLookup<?> mapKey) {
        System.out.println(mapKey.getToken());
        return super.visitFlowKeyLookup(mapKey);
    }

    @Override
    public Term<?> visitPrimitive(Primitive<?> primitive) {
        return super.visitPrimitive(primitive);
    }

    @Override
    public Term<?> visitConcatenation(Concatenation concat) {
        return super.visitConcatenation(concat);
    }

    @Override
    public Term<?> visitListTerm(ListTerm<?> list) {
        return super.visitListTerm(list);
    }

    @Override
    public Term<?> visitMapTerm(MapTerm<?> mapTerm) {
        return super.visitMapTerm(mapTerm);
    }

    @Override
    public Term<?> visitListLookup(ListLookup<?> listLookup) {
        return super.visitListLookup(listLookup);
    }

    @Override
    public Term<?> visitMapLookup(MapLookup<?> mapLookup) {
        return super.visitMapLookup(mapLookup);
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // TODO
    private T<?> captureReplace(T<?> t1, Map<String, T<?>> a) {
        Type topLevel = new ReplaceCapturesOrCrashVisitor(a).visit(t1.get());
        T newT = new T<>(topLevel){};
        newT.setTerm(t1.getTerm());
        Term replaced = (Term) newT.getTerm().accept(new ReplaceCaptures(a));
        newT.setTerm(replaced);

        return newT;
    }
}
