package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.exceptions.TemplateException;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.reflect.Primitive;
import scraper.api.reflect.T;
import scraper.api.reflect.TVisitor;

import java.util.Map;

import static scraper.core.converter.StringToClassConverter.convert;

public class TemplateConstant<K> extends TemplateExpression<K> implements Primitive<K> {
    @Override public void accept(TVisitor visitor) { visitor.visitPrimitive(this); }
    private final K constant;

    public TemplateConstant(K constant, T<K> targetType) {
        super(targetType);
        this.constant = constant;
    }

    public K eval(@NotNull final FlowMap o) {
        return constant;
    }

    @Override
    public Object getRaw() {
        return constant;
    }

    @Override
    public Map<String, T<?>> getKeysInTemplate(FlowMap o) {
        return null;
    }

    public String toString() {
        return constant.toString();
    }

}
