package scraper.api.node;

import com.google.common.reflect.TypeToken;

/**
 * Interface to implement type safe objects with generics
 *
 * @since 1.0.0
 */
public interface TypesafeObject {
    TypeToken<?> getType();
}
