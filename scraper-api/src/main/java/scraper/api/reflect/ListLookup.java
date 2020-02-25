package scraper.api.reflect;

import java.util.List;

public interface ListLookup<Y> extends Term<Y> {
    Term<List<? extends Y>> getListObjectTerm();

    Term<Integer> getIndexTerm();
}
