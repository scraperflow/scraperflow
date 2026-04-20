package scraper.api;

import scraper.annotations.NotNull;

public interface Primitive<Y> extends Term<Y> {
    /** Primitives can eval without the need of a FlowMap */
    @NotNull Y eval();
    boolean isTypeVariable();

}
