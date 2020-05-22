package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.template.Primitive;
import scraper.api.template.T;
import scraper.api.template.TVisitor;

import static scraper.core.converter.StringToClassConverter.convert;

public class TemplateString<K> extends TemplateExpression<K> implements Primitive<K> {
    @Override public void accept(@NotNull TVisitor visitor) { visitor.visitPrimitive(this); }

    private @NotNull final StringBuilder stringContent = new StringBuilder();

    public TemplateString(T<K> targetType) {
        super(targetType);
    }

    public K eval(@NotNull FlowMap o) {
        return eval();
    }

    @NotNull
    @Override
    public Object getRaw() {
        return toString();
    }

    @NotNull
    @Override
    public K eval() {
        try {
            @SuppressWarnings({"RedundantCast", "unchecked"}) // lost type but reconstructed
            K result = (K) convert(stringContent.toString(), TypeToken.of(targetType.get()).getRawType());
            return result;
        } catch (ValidationException e) {
            throw new TemplateException("Could not convert string content to " + targetType.get());
        }
    }

    @Override
    public String toString() {
        return stringContent.toString();
    }

    public void addContent(Object content) {
        stringContent.append(content.toString());
    }
}
