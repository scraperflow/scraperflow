package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.template.MapLookup;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.Map;
import java.util.Objects;

import static scraper.core.converter.StringToClassConverter.convert;

public class TemplateMapLookup<K> extends TemplateExpression<K> implements MapLookup<K> {
    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitMapLookup(this); }

    private TemplateExpression<Map<String, K>> map;
    private TemplateExpression<String> key;

    public TemplateMapLookup(
            TemplateExpression<Map<String, K>> map,
            TemplateExpression<String> key,
            T<K> targetType) {
        super(targetType);
        this.map = map;
        this.key = key;
    }

    @SuppressWarnings("unchecked") // checked with map generics subtype relation
    public K eval(@NotNull final FlowMap o) {
        try {
            Map<String, ?> m = map.eval(o);
            String k = key.eval(o);

            Object mapElement = m.get(k);

            if(mapElement == null)
                throw new TemplateException("Key '"+k+"' does not exist for map access '" +toString() +"'. Map has only the keys " + m.keySet()+"");


//            TypeToken<?> known = FlowMapImpl.inferType(mapElement);
//            TypeToken<?> target = TypeToken.of(targetType.get());
//
//            FlowMapImpl.checkGenericType(known, target);

            return (K) convert(mapElement, TypeToken.of(targetType.get()).getRawType());
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
