package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.template.ListTerm;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TemplateList<K> implements ListTerm<K> {

    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitListTerm(this); }

    private final List<Term<K>> termList;
    private T<List<K>> listType;
    private final boolean fromTypeVariable;
    private final T<K> elementType;

    @Override
    public T<K> getElementType() {
        return elementType;
    }

    @Override @NotNull
    public List<Term<K>> getTerms(){ return termList; }

    @Override
    public boolean isTypeVariable() {
        return fromTypeVariable;
    }

    @Override
    public Term<List<K>> withTypeVar(String suffix) {
        return this;
    }

    public TemplateList(@NotNull List<Term<K>> termList, @NotNull T<List<K>> listType, T<K> elementType, boolean fromTypeVariable) {
        this.termList = termList;
        this.listType = listType;
        this.elementType = elementType;
        this.fromTypeVariable = fromTypeVariable;
    }

    public @NotNull List<K> eval(@NotNull final FlowMap o) {
        List<K> result = new ArrayList<>();
        termList.forEach(t -> result.add(t.eval(o)));
        return result;
    }

    @NotNull
    @Override
    public Object getRaw() {
        return termList.stream().map(Term::getRaw).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public T<List<K>> getToken() {
        return listType;
    }

    @Override
    public void setToken(T<List<K>> t) {
        listType = t;
    }

    @Override
    public String getTypeString() {
        return listType.get().getTypeName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateList<?> that = (TemplateList<?>) o;
        return termList.equals(that.termList) &&
                listType.get().equals(that.listType.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(termList, listType.get());
    }
}
