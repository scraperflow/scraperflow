package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.flow.FlowMap;
import scraper.api.template.FlowKeyLookup;
import scraper.api.template.T;
import scraper.api.template.TVisitor;
import scraper.api.template.Term;

import java.util.*;

public class TemplateMapKey<K> extends TemplateExpression<K> implements FlowKeyLookup<K> {
    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitFlowKeyLookup(this); }


    @Override public int getTypevarindex() { throw new IllegalStateException(); }
    @NotNull
    @Override
    public Term<String> getKeyLookup() { return keyLookup; }

    private final boolean fromTypeVariable;

    @Override
    public boolean isTypeVariable() {
        return fromTypeVariable;
    }

    private final Term<String> keyLookup;

    @Override
    public Term<K> withTypeVar(String suffix) {
        return this;
    }

    public TemplateMapKey(@NotNull final TemplateExpression<String> keyLookup, @NotNull final T<K> targetType, boolean fromTypeVariable) {
        super(targetType);
        this.keyLookup = keyLookup;
        this.fromTypeVariable = fromTypeVariable;
    }

    @Override
    public String getTypeString() {
        return targetType.get().getTypeName();
    }

    @SuppressWarnings("unchecked") // statically checked
    public K eval(@NotNull final FlowMap o) {
        try{
            String targetKey = keyLookup.eval(o);
            Optional<K> targetValue = (Optional<K>) o.get(targetKey);

            if(targetValue.isEmpty())
                throw new IllegalStateException("FlowMap has no element at key " + targetKey);

            return targetValue.get();
        } catch (Exception e) {
            throw new TemplateException(e, "Could not evaluate map key template '"+toString()+"'. "+ e.toString());
        }
    }

    @NotNull
    @Override
    public Object getRaw() {
        return toString();
    }

    @Override
    public String toString() {
        return "{" + keyLookup.getRaw().toString() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateMapKey<?> that = (TemplateMapKey<?>) o;
        return keyLookup.equals(that.keyLookup);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyLookup);
    }
}
