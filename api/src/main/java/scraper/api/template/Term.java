package scraper.api.template;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.flow.FlowMap;

import java.util.List;

public interface Term<Y> {
    <X> X accept(@NotNull TVisitor<X> visitor);
    @Nullable Y eval(@NotNull FlowMap o);

    @NotNull Object getRaw();
    @NotNull T<Y> getToken();
    void setToken(T<Y> t);
    String getTypeString();
    boolean isTypeVariable();

    // propagates type var down this term
    Term<Y> withTypeVar(String suffix);
}
