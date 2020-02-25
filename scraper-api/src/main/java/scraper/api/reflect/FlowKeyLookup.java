package scraper.api.reflect;

public interface FlowKeyLookup<Y> extends Term<Y> {

    Term<String> getKeyLookup();
}
