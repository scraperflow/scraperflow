package scraper.core.template;

import com.google.common.reflect.TypeToken;
import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.Primitive;
import scraper.api.reflect.T;
import scraper.api.reflect.TVisitor;

import java.util.Map;

import static scraper.core.converter.StringToClassConverter.convert;

public class TemplateString<K> extends TemplateExpression<K> implements Primitive<K> {
    @Override public void accept(TVisitor visitor) { visitor.visitPrimitive(this); }

    private StringBuilder stringContent = new StringBuilder();

    public TemplateString(T<K> targetType) {
        super(targetType);
    }

    public K eval(@NotNull final FlowMap o) {
        try {
            @SuppressWarnings({"RedundantCast", "unchecked"}) // lost type but reconstructed
            K result = (K) convert(stringContent.toString(), TypeToken.of(targetType.get()).getRawType());
            return result;
        } catch (ValidationException e) {
            throw new TemplateException("Could not convert string content to " + targetType);
        }
    }

    @Override
    public Object getRaw() {
        return toString();
    }

    public String toString() {
        return stringContent.toString();
    }

    @NotNull
    public Map<String, T<?>> getKeysInTemplate(@NotNull FlowMap o ) {
        throw new IllegalStateException();
//        return Map.of(stringContent.toString(), new T<>(targetType.getType()){});
    }

    public void addContent(Object content) {
        stringContent.append(content.toString());
    }

}
