package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.MapOrListLookup;
import scraper.api.reflect.T;
import scraper.api.reflect.TVisitor;
import scraper.api.reflect.Term;

import java.util.List;
import java.util.Map;

public class TemplateMapOrListLookup<K> extends TemplateExpression<K> implements MapOrListLookup<K> {
    @Override public void accept(TVisitor visitor) { visitor.visitMapOrListLookup(this); }

    private TemplateExpression<List<? extends K>> list;
    private TemplateExpression<Integer> index;
    private TemplateExpression<Map<String, ? extends K>> map;
    private TemplateExpression<String> key;

    public TemplateMapOrListLookup(
            TemplateExpression<Map<String, ? extends K>> map,
            TemplateExpression<List<? extends K>> list,
            TemplateExpression<Integer> index,
            TemplateExpression<String> key,
            T<K> targetType) {
        super(targetType);
        this.map = map;
        this.list = list;
        this.index = index;
        this.key = key;
    }



    public K eval(@NotNull final FlowMap o) {

        String otherMsg;
        try{
            List<? extends K> l = list.eval(o);
            Integer index = this.index.eval(o);
            K element;

            if (index < 0) {
                element = l.get(l.size() - Math.abs(index));
            } else {
                element =  l.get(index);
            }
            return element;
        }
        catch (IndexOutOfBoundsException e) {
            throw new TemplateException("Array index out of bounds for '"+toString()+"': "+ e.getMessage());
        }
        catch (Exception e) {
            otherMsg = e.getMessage();
        }

        try {
            Map<String, ? extends K> m = map.eval(o);
            String k = key.eval(o);

            K mapElement = m.get(k);

            if(mapElement == null) throw new TemplateException("Key '"+k+"' does not exist for map access '" +toString() +"'. Map has only the keys " + m.keySet()+"");

            return mapElement;
        } catch (Exception e) {
            throw new TemplateException("Could not evaluate array/map lookup template '"+toString()+"'. " + e.getMessage()+ " // " + otherMsg);
        }
    }

    @Override
    public Object getRaw() {
        return toString();
    }

    @Override
    public String toString() {
        return "{" + list.toString() + "}[" + index.toString() + "]";
    }

    public @NotNull Map<String, T<?>> getKeysInTemplate(@NotNull FlowMap o) {
        // TODO implement
        throw new IllegalStateException("Not implemented yet");
    }

    @Override
    public Term<List<? extends K>> getListObjectTerm() {
        return list;
    }

    @Override
    public Term<Integer> getIndexTerm() {
        return index;
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
