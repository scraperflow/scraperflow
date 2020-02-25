package scraper.api.reflect;

import java.util.List;

public interface Concatenation<Y> extends Term<Y> {

    List<Term<String>> getConcatTemplatesOrStrings();
}
