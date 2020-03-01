package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.template.ListLookup;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.List;

public class TemplateListLookup<K> extends TemplateExpression<K> implements ListLookup<K> {
    @Override public void accept(@NotNull TVisitor visitor) { visitor.visitListLookup(this); }

    private TemplateExpression<List<K>> list;
    private TemplateExpression<Integer> index;

    public TemplateListLookup(
            TemplateExpression<List<K>> list,
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

    @NotNull
    @Override
    public Object getRaw() {
        return toString();
    }

    @Override
    public String toString() {
        return "{" + list.toString() + "}[" + index.toString() + "]";
    }

    @NotNull
    @Override
    public Term<List<K>> getListObjectTerm() {
        return list;
    }

    @NotNull
    @Override
    public Term<Integer> getIndexTerm() {
        return index;
    }
}
