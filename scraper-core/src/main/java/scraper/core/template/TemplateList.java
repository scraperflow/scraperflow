package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.ListTerm;
import scraper.api.reflect.T;
import scraper.api.reflect.TVisitor;
import scraper.api.reflect.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static scraper.util.TemplateUtil.listOf;

public class TemplateList<K> implements ListTerm<K> {
    @Override public void accept(TVisitor visitor) { visitor.visitListTerm(this); }

    private final List<Term<K>> termList;
    private final T<K> elementType;
    private final T<List<K>> listType;

    @Override
    public List<Term<K>> getTerms(){
        return termList;
    }

    public TemplateList(List<Term<K>> termList, T<K> elementType, T<List<K>> listType) {
        this.termList = termList;
        this.elementType = elementType;
        this.listType = listType;
    }

    public List<K> eval(@NotNull final FlowMap o) {
        List<K> result = new ArrayList<>();
        termList.forEach(t -> result.add(t.eval(o)));
        return result;
    }

    @Override
    public Object getRaw() {
        return termList.stream().map(Term::getRaw).collect(Collectors.toList());
    }

    @Override
    public T<List<K>> getToken() {
        return listType;
    }

    @NotNull
    public Map<String, T<?>> getKeysInTemplate(@NotNull FlowMap o ) {
        throw new IllegalStateException();
//        return Map.of(stringContent.toString(), new T<>(targetType.getType()){});
    }

    @Override
    public T<K> getElementToken() {
        return elementType;
    }
}
