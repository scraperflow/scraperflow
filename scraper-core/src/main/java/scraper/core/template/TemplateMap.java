package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateMap<K> implements MapTerm<K> {


    @Override public void accept(TVisitor visitor) { visitor.visitMapTerm(this); }

    private final Map<String, Term<K>> termMap;
    private final T<K> elementType;
    private final T<Map<String, K>> mapType;

    @Override
    public Map<String, Term<K>> getTerms(){
        return termMap;
    }

    public TemplateMap(Map<String, Term<K>> termMap, T<K> targetType, T<Map<String, K>> mapType) {
        this.termMap = termMap;
        this.elementType = targetType;
        this.mapType = mapType;
    }

    public Map<String, K> eval(@NotNull final FlowMap o) {
        Map<String, K> result = new HashMap<>();
        termMap.forEach((k,v) -> result.put(k, v.eval(o)));
        return result;
    }

    @Override
    public Object getRaw() {
        return termMap.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(),e.getValue().getRaw()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Override
    public T<Map<String, K>> getToken() {
        return mapType;
    }

    @NotNull
    public Map<String, T<?>> getKeysInTemplate(@NotNull FlowMap o ) {
        throw new IllegalStateException();
//        return Map.of(stringContent.toString(), new T<>(targetType.getType()){});
    }

}
