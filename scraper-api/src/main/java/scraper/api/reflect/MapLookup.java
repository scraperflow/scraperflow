package scraper.api.reflect;

import java.util.Map;

public interface MapLookup<Y> extends Term<Y> {
    Term<Map<String, ? extends Y>> getMapObjectTerm();

    Term<String> getKeyTerm();
}
