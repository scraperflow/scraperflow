package scraper.utils;

import java.util.function.Supplier;

public final class LambdaUtil {
    public static<T> Supplier<T> combine(SupplierEx<T> b) {
        return () -> {
            try {
                return b.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }


    @FunctionalInterface
    public
    interface SupplierEx<T> {
        T get() throws Exception;
    }
}
