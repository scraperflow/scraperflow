package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.FlowMap;
import scraper.api.MapTerm;
import scraper.api.T;
import scraper.api.TVisitor;
import scraper.api.Term;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TemplateMap<K> implements MapTerm<K> {

    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitMapTerm(this); }

    private final Map<String, Term<K>> termMap;
    private final boolean fromTypeVariable;

    @Override
    public T<K> getElementType() {
        return elementType;
    }

    private T<K> elementType;
    private T<Map<String, K>> mapType;

    @Override
    public boolean isTypeVariable() {
        return fromTypeVariable;
    }

    @Override
    public Term<Map<String, K>> withTypeVar(String suffix) {
        return this;
    }

    @NotNull
    @Override
    public Map<String, Term<K>> getTerms(){
        return termMap;
    }

    @Override
    public String getTypeString() {
//        if(fromTypeVariable) {
//            return "Map<String, "+elementType.getTypeString()+">";
//        } else {
        return mapType.get().getTypeName();
//        }
    }

    public TemplateMap(Map<String, Term<K>> termMap, T<K> elementType, T<Map<String, K>> targetType, boolean fromTypeVariable) {
        this.termMap = termMap;
        this.elementType = elementType;
        this.mapType = targetType;
        this.fromTypeVariable = fromTypeVariable;
    }

    public Map<String, K> eval(@NotNull final FlowMap o) {
        Map<String, K> result = new HashMap<>();
        termMap.forEach((k,v) -> result.put(k, v.eval(o)));
        return result;
    }

    @NotNull
    @Override
    public Object getRaw() {
        return termMap.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(),e.getValue().getRaw()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @NotNull
    @Override
    public T<Map<String, K>> getToken() {
        return mapType;
    }

    @Override
    public void setToken(T<Map<String, K>> t) {
        mapType = t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateMap<?> that = (TemplateMap<?>) o;
        return termMap.equals(that.termMap) &&
                elementType.equals(that.elementType) &&
                mapType.get().equals(that.mapType.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(termMap, elementType, mapType.get());
    }
}
