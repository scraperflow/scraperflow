package scraper.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class ClassUtil {
//    /** Concatenates all simple object names of given list */
//    public static String getSimpleNames(final List objectList) {
//        StringBuilder arrayString = new StringBuilder("[");
//        for (Object o : objectList) {
//            arrayString.append(o.getClass().getSimpleName()).append(", ");
//        }
//
//        if(arrayString.length() == 1) return "[]";
//
//        return arrayString.substring(0, arrayString.length() -2)+"]";
//    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    /** Extracts the plugin category, defined as the first package name after the 'nodes' package */
//    public static String extractCategoryOfNode(Class<?> plugin) {
//        return extractCategoryOfNode(plugin.getName());
//    }


    public static String extractCategoryOfNode(String plugin) {
        int index = plugin.indexOf("nodes");
        if(index == -1) return "NotANode";

        int index2 = plugin.indexOf(".", index+6);

        return plugin.substring(index+6, Math.min(index2, plugin.length()));
    }

    @SuppressWarnings("unchecked") // sneaky
    public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
