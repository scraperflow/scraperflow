package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.MapLookup;
import scraper.api.reflect.T;
import scraper.api.reflect.TVisitor;
import scraper.api.reflect.Term;

import java.util.Map;

public class TemplateMapLookup<K> extends TemplateExpression<K> implements MapLookup<K> {
    @Override public void accept(TVisitor visitor) { visitor.visitMapLookup(this); }

    private TemplateExpression<Map<String, ? extends K>> map;
    private TemplateExpression<String> key;

    public TemplateMapLookup(
            TemplateExpression<Map<String, ? extends K>> map,
            TemplateExpression<String> key,
            T<K> targetType) {
        super(targetType);
        this.map = map;
        this.key = key;
    }



    public K eval(@NotNull final FlowMap o) {
        try {
            Map<String, ? extends K> m = map.eval(o);
            String k = key.eval(o);

            K mapElement = m.get(k);

            if(mapElement == null) throw new TemplateException("Key '"+k+"' does not exist for map access '" +toString() +"'. Map has only the keys " + m.keySet()+"");

            return mapElement;
        } catch (Exception e) {
            throw new TemplateException(e, "Could not evaluate array/map lookup template '"+toString()+"'. " + e.getMessage());
        }
    }

    @Override
    public Object getRaw() {
        return toString();
    }

    @Override
    public String toString() {
        return "{" + map.getRaw() + "}[" + key.getRaw() + "]";
    }

    public @NotNull Map<String, T<?>> getKeysInTemplate(@NotNull FlowMap o) {
        // TODO implement
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Term<Map<String, ? extends K>> getMapObjectTerm() {
        return map;
    }

    @Override
    public Term<String> getKeyTerm() {
        return key;
    }
}
