package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.TemplateException;
import scraper.api.FlowMap;
import scraper.api.ListLookup;
import scraper.api.T;
import scraper.api.TVisitor;
import scraper.api.Term;

import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Objects;

import static scraper.core.converter.StringToClassConverter.convert;

public class TemplateListLookup<K> extends TemplateExpression<K> implements ListLookup<K> {
    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitListLookup(this); }

    private final TemplateExpression<List<K>> list;
    private final TemplateExpression<Integer> index;

    public TemplateListLookup(
            TemplateExpression<List<K>> list,
            TemplateExpression<Integer> index,
            T<K> targetType) {
        super(targetType);
        this.list = list;
        this.index = index;
    }

    @Override
    public boolean isTypeVariable() {
        return getToken().get() instanceof TypeVariable;
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // checked with generics subtype relation
    public K eval(@NotNull final FlowMap o) {
        try{
            List l = list.eval(o);
            Integer index = this.index.eval(o);
            Object element;

            if (index < 0) {
                element = l.get(l.size() - Math.abs(index));
            } else {
                element = l.get(index);
            }

            return (K) convert(element, targetType.getRawType());
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
    public String getTypeString() {
        return getListObjectTerm().getTypeString();
    }


    @Override
    public String getTypeStringElement() {
        return getTypeString().substring(15, getTypeString().length()-1);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateListLookup<?> that = (TemplateListLookup<?>) o;
        return list.equals(that.list) &&
                index.equals(that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, index);
    }
}
