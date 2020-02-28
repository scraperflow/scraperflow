package scraper.api.template;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.flow.FlowMap;

public interface Term<Y> {
    void accept(@NotNull TVisitor visitor);
    @Nullable Y eval(@NotNull FlowMap o);
    @NotNull Object getRaw();
    @NotNull T<Y> getToken();
}
