package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.ListLookup;
import scraper.api.reflect.T;
import scraper.api.reflect.TVisitor;
import scraper.api.reflect.Term;

import java.util.List;
import java.util.Map;

public class TemplateListLookup<K> extends TemplateExpression<K> implements ListLookup<K> {
    @Override public void accept(TVisitor visitor) { visitor.visitListLookup(this); }

    private TemplateExpression<List<? extends K>> list;
    private TemplateExpression<Integer> index;

    public TemplateListLookup(
            TemplateExpression<List<? extends K>> list,
            TemplateExpression<Integer> index,
            T<K> targetType) {
        super(targetType);
        this.list = list;
        this.index = index;
    }



    public K eval(@NotNull final FlowMap o) {
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
            throw new TemplateException(e, "Array index out of bounds for '"+toString()+"': "+ e.getMessage());
        }
        catch (Exception e) {
            throw new TemplateException(e, "Could not evaluate list access: "+ e.getMessage());
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
}
