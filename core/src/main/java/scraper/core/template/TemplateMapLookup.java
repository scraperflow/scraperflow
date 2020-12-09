package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.template.MapLookup;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.Map;
import java.util.Objects;

import static scraper.api.template.T.rawType;
import static scraper.core.converter.StringToClassConverter.convert;

public class TemplateMapLookup<K> extends TemplateExpression<K> implements MapLookup<K> {

    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitMapLookup(this); }

    private TemplateExpression<Map<String, K>> map;
    private TemplateExpression<String> key;
    private final boolean isTypeVar;
    private String typevarsuffix = "";

    @Override
    public boolean isTypeVariable() {
        return isTypeVar;
    }

    @Override
    public Term<K> withTypeVar(String typevarsuffix) {
        this.typevarsuffix = typevarsuffix;
        return this;
    }

    public TemplateMapLookup(
            TemplateExpression<Map<String, K>> map,
            TemplateExpression<String> key,
            T<K> targetType,
            boolean isTypeVariable
            ) {
        super(targetType);
        this.map = map;
        this.key = key;
        this.isTypeVar = isTypeVariable;
    }

    @Override
    public String getTypeString() {
        return map.targetType.getTypeString() + (typevarsuffix.isEmpty() ? "" : "$"+this.typevarsuffix);
    }

    @SuppressWarnings("unchecked") // checked with map generics subtype relation
    public K eval(@NotNull final FlowMap o) {
        try {
            Map<String, ?> m = map.eval(o);
            String k = key.eval(o);

            Object mapElement = m.get(k);

            if(mapElement == null)
                throw new TemplateException("Key '"+k+"' does not exist for map access '" +toString() +"'. Map has only the keys " + m.keySet()+"");

            return (K) convert(mapElement, rawType(targetType.get()));
        } catch (Exception e) {
            throw new TemplateException(e, "Could not evaluate array/map lookup template '"+toString()+"'. " + e.getMessage());
        }
    }

    @NotNull
    @Override
    public Object getRaw() {
        return toString();
    }

    @Override
    public String toString() {
        return "{" + map.getRaw() + "@" + key.getRaw()+ "}";
    }

    @NotNull
    @Override
    public Term<Map<String, K>> getMapObjectTerm() {
        return map;
    }

    @NotNull
    @Override
    public Term<String> getKeyTerm() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateMapLookup<?> that = (TemplateMapLookup<?>) o;
        return map.equals(that.map) &&
                key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map, key);
    }
}
