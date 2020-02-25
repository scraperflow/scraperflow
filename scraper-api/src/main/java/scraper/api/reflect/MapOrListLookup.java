package scraper.api.reflect;

import java.util.List;
import java.util.Map;

public interface MapOrListLookup<Y> extends Term<Y> {

    Term<List<? extends Y>> getListObjectTerm();

    Term<Integer> getIndexTerm();

    Term<Map<String, ? extends Y>> getMapObjectTerm();

    Term<String> getKeyTerm();
}
