package scraper.utils;

import scraper.annotations.NotNull;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public final class ClassUtil {
    private ClassUtil(){}

    /** Returns all fields of a given class, including the fields of all super classes */
    public static @NotNull List<Field> getAllFields(@NotNull final List<Field> fields, @NotNull final Class<?> type) {
        Field[] toAdd = type.getDeclaredFields();
        for (Field field : toAdd) {
            if (!fields.stream().map(Field::getName).collect(Collectors.toList()).contains(field.getName())) {
                fields.add(field);
            }
        }
        if (type.getSuperclass() != null) getAllFields(fields, type.getSuperclass());
        return fields;
    }

    /** Tries to extract the category of a fully qualified node name */
    public static @NotNull String extractCategoryOfNode(@NotNull final String fullyQualifiedNodeClassName) {
        int index = fullyQualifiedNodeClassName.indexOf("nodes");
        if(index == -1) return "NotANode";

        int index2 = fullyQualifiedNodeClassName.indexOf(".", index+6);
        try {
            return fullyQualifiedNodeClassName.substring(index+6, Math.min(index2, fullyQualifiedNodeClassName.length()));
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Trying to extract a category, unexpected bounds of substring encountered for " + fullyQualifiedNodeClassName);
            return "unknown";
        }
    }

    /** Throws any exception unchecked */
    @SuppressWarnings("unchecked") // sneaky
    public static <E extends Throwable> void sneakyThrow(@NotNull final Throwable e) throws E {
        throw (E) e;
    }

    /** Sleep with runtime throw of the interrupted exception */
    public static void sleep(int timems) {
        try {
            Thread.sleep(timems);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static URL getResourceUrl(Class<?> baseClass, String ok) {
        String base = baseClass.getPackageName().replace(".","/" ) + "/";
        return ClassLoader.getSystemResource(base + ok);
    }
}
