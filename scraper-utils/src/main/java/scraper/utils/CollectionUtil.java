package scraper.utils;

import scraper.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CollectionUtil {
    private CollectionUtil(){}

    /** Creates a shallow copy of the given list and inserts new elements */
    @NotNull
    public static <T> List<T> newAppend(@NotNull final List<T> list, @NotNull final T[] args){
        List<T> newList = new ArrayList<>(list);
        newList.addAll(Arrays.asList(args));
        return newList;
    }

    /** Creates a shallow copy of the given list and inserts the new element */
    @NotNull
    public static <T> List<T> newAppend(@NotNull final List<T> list, @NotNull final T arg){
        List<T> newList = new ArrayList<>(list);
        newList.add(arg);
        return newList;
    }
}
