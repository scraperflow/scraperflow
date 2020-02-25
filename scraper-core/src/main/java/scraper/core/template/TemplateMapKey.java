package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.FlowKeyLookup;
import scraper.api.reflect.T;
import scraper.api.reflect.TVisitor;
import scraper.api.reflect.Term;

import java.util.*;

public class TemplateMapKey<K> extends TemplateExpression<K> implements FlowKeyLookup<K> {
    @Override public void accept(TVisitor visitor) { visitor.visitFlowKeyLookup(this); }

    @Override
    public Term<String> getKeyLookup() { return keyLookup; }
    private Term<String> keyLookup;

    public TemplateMapKey(@NotNull final TemplateExpression<String> keyLookup, @NotNull final T<K> targetType) {
        super(targetType);
        this.keyLookup = keyLookup;
    }

    public K eval(@NotNull final FlowMap o) {
        try{
            String targetKey = keyLookup.eval(o);
            Optional<K> targetValue = o.getWithType(targetKey, targetType);

            if(targetValue.isEmpty())
                throw new IllegalStateException("FlowMap has no element at key " + targetKey);

            return targetValue.get();
        } catch (Exception e) {
            throw new TemplateException(e, "Could not evaluate map key template '"+toString()+"'. "+ e.toString());
        }
    }

    @Override
    public Object getRaw() {
        return toString();
    }

    @Override
    public String toString() {
        return "{" + keyLookup.getRaw().toString() + "}";
    }

    @NotNull
    public Map<String, T<?>> getKeysInTemplate(@NotNull FlowMap o) {
//        Map<String, T<?>> allKeys = new HashMap<>(keyLookup.getKeysInTemplate(o));
//        keyLookup.getKeysInTemplate(o).forEach((k,v) -> {
//            // TODO better error message
//            if(allKeys.containsKey(k) && !allKeys.get(k).equals(v))
//                throw new IllegalStateException("Types don't match");
//            allKeys.put(k,v);
//        });
//
//        // TODO this evaluates the key lookup part
//        //      what if this is not known at compile time?
//        String location = keyLookup.eval(o);
//        allKeys.put(location, new T<>(targetType.getType()){});

        return null;
    }
}
