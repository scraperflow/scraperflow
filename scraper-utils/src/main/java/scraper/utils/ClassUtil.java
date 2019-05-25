package scraper.utils;

import scraper.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class ClassUtil {
    private ClassUtil(){}

    /** Returns all fields of a given class, including the fields of all super classes */
    @NotNull
    public static List<Field> getAllFields(@NotNull final List<Field> fields, @NotNull final Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    /** Tries to extract the category of a fully qualified node name */
    @NotNull
    public static String extractCategoryOfNode(@NotNull final String fullyQualifiedNodeClassName) {
        int index = fullyQualifiedNodeClassName.indexOf("nodes");
        if(index == -1) return "NotANode";

        int index2 = fullyQualifiedNodeClassName.indexOf(".", index+6);
        try {
            return fullyQualifiedNodeClassName.substring(index+6, Math.min(index2, fullyQualifiedNodeClassName.length()));
        } catch (IndexOutOfBoundsException e) {
            return "unknown";
        }
    }

    /** Throws any exception unchecked */
    @SuppressWarnings("unchecked") // sneaky
    public static <E extends Throwable> void sneakyThrow(@NotNull final Throwable e) throws E {
        throw (E) e;
    }

    public static void sleep(int timems) {
        try {
            Thread.sleep(timems);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
