package scraper.api.reflect;

import scraper.api.flow.FlowMap;

public interface Term<Y> {
    void accept(TVisitor visitor);
    Y eval(FlowMap o);
    Object getRaw();
    T<Y> getToken();
}
