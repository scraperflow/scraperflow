package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.template.Primitive;
import scraper.api.template.T;
import scraper.api.template.TVisitor;

import java.util.Objects;

import static scraper.core.converter.StringToClassConverter.convert;

public class TemplateConstant<K> extends TemplateExpression<K> implements Primitive<K> {
    @Override public <X> X accept(@NotNull TVisitor<X> visitor) { return visitor.visitPrimitive(this); }
    private final Object constant;

    public TemplateConstant(@NotNull Object constant, @NotNull T<K> targetType) {
        super(targetType);
        this.constant = constant;
    }

    public K eval(@NotNull final FlowMap o) {
        return eval();
    }

    @NotNull
    @Override
    public Object getRaw() {
        return toString();
    }

    @Override
    public String toString() {
        return constant.toString();
    }

    @NotNull
    @Override
    public K eval() {
        try {
            @SuppressWarnings({"RedundantCast", "unchecked"}) // lost type but reconstructed
            K result = (K) convert(constant, TypeToken.of(targetType.get()).getRawType());
            return result;
        } catch (ValidationException e) {
            throw new TemplateException("Could not convert constant '"+constant+"' to " + targetType.get());
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateConstant<?> that = (TemplateConstant<?>) o;
        return constant.equals(that.constant);
    }

    @Override public int hashCode() { return Objects.hash(constant); }
}
