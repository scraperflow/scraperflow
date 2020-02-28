package scraper.core.template;

import scraper.annotations.NotNull;
import scraper.api.flow.FlowMap;
import scraper.api.template.Primitive;
import scraper.api.template.T;
import scraper.api.template.TVisitor;

public class TemplateConstant<K> extends TemplateExpression<K> implements Primitive<K> {
    @Override public void accept(@NotNull TVisitor visitor) { visitor.visitPrimitive(this); }
    private final K constant;

    public TemplateConstant(@NotNull K constant, @NotNull T<K> targetType) {
        super(targetType);
        this.constant = constant;
    }

    public K eval(@NotNull final FlowMap o) {
        return eval();
    }

    @NotNull
    @Override
    public Object getRaw() {
        return constant;
    }

    @Override
    public String toString() {
        return constant.toString();
    }

    @NotNull
    @Override
    public K eval() {
        return constant;
    }
}
