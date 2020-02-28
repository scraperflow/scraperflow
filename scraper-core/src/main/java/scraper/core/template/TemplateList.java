package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.template.ListTerm;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateList<K> implements ListTerm<K> {
    @Override public void accept(@NotNull TVisitor visitor) { visitor.visitListTerm(this); }

    private final List<Term<K>> termList;
    private final T<List<K>> listType;

    @Override @NotNull
    public List<Term<K>> getTerms(){ return termList; }

    public TemplateList(@NotNull List<Term<K>> termList, @NotNull T<List<K>> listType) {
        this.termList = termList;
        this.listType = listType;
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
}
