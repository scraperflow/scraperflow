package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.TemplateException;
import scraper.api.FlowMap;
import scraper.api.flow.impl.FlowMapImpl;
import scraper.api.FlowKeyLookup;
import scraper.api.T;
import scraper.api.TVisitor;
import scraper.api.Term;

import java.util.*;

public class TemplateMapKey<K> extends TemplateExpression<K> implements FlowKeyLookup<K> {

    private final boolean consume;

    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitFlowKeyLookup(this); }

    @NotNull
    @Override
    public Term<String> getKeyLookup() { return keyLookup; }

    private final boolean fromTypeVariable;

    @Override
    public boolean isTypeVariable() {
        return fromTypeVariable;
    }

    @Override
    public boolean isConsume() {
        return consume;
    }

    private final Term<String> keyLookup;

    @Override
    public Term<K> withTypeVar(String suffix) {
        return this;
    }

    public TemplateMapKey(TemplateExpression<String> keyLookup, T<K> targetType, boolean fromTypeVariable, boolean b) {
        super(targetType);
        this.keyLookup = keyLookup;
        this.fromTypeVariable = fromTypeVariable;
        this.consume = b;
    }
    
    public TemplateMapKey(@NotNull final TemplateExpression<String> keyLookup, @NotNull final T<K> targetType, boolean fromTypeVariable) {
        this(keyLookup, targetType, fromTypeVariable, false);
    }

    @Override
    public String getTypeString() {
        return targetType.get().getTypeName();
    }

    @SuppressWarnings("unchecked") // statically checked
    public K eval(@NotNull final FlowMap o) {
        try{
            // TODO how to get rid of the FlowMapImpl cast
            String targetKey = keyLookup.eval(o);
            Optional<K> targetValue = (Optional<K>) Optional.ofNullable(((FlowMapImpl) o).getPrivateMap().get(targetKey));

            if(targetValue.isEmpty())
                throw new IllegalStateException("FlowMap has no element at key " + keyLookup.eval(o));

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
