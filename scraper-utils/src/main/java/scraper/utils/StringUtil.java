package scraper.utils;


import org.apache.commons.io.FilenameUtils;
import scraper.annotations.NotNull;
import scraper.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess") // util class
public final class StringUtil {
    private StringUtil(){}

    /** Removes the extension of a filename */
    @NotNull
    public static String removeExtension(@NotNull final String filename) {
        return FilenameUtils.removeExtension(filename);
    }

    /** Reads the body of a file and concatenates the lines with the newline separator */
    @NotNull
    public static String readBody(@NotNull final File data) throws IOException {
        return readBody(data, System.getProperty("line.separator"));
    }

    /** Reads the body of a file and concatenates the lines with chosen separator */
    @NotNull
    public static String readBody(@NotNull final File data, @NotNull final String separator) throws IOException {
        StringBuilder body = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(data.getAbsolutePath()))) {
            stream.forEachOrdered(line -> body.append(line).append(separator));
        }
        return body.toString();
    }

    /** Reads the body of a file and returns an ordered list */
    @NotNull
    public static List<String> readBodyToList(@NotNull final File data) throws IOException {
        List<String> body = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(data.getAbsolutePath()))) {
            stream.forEachOrdered(body::add);
        }
        return body;
    }

    /** Reads a file and applies an action for each line */
    public static void readBody(@NotNull final File data, @NotNull final Consumer<String> action) throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(data.getAbsolutePath()))) {
            stream.forEachOrdered(action);
        }
    }

    /** Returns the first argument starting with given parameter and delimiter ':', or null if none found */
    @Nullable
    public static String getArgument(@NotNull final String[] args, @NotNull final String param){
        for (String argument: args) {
            if(argument.startsWith(param)){
                String arg =  argument.substring(param.length());
                if (arg.equalsIgnoreCase("null"))
                    return null;

                return arg;
            }
        }

        return null;
    }

    /** Returns all arguments starting with given parameter and delimiter ':' */
    @NotNull
    public static List<String> getAllArguments(@NotNull final String[] args, @NotNull final String param){
        List<String> arg = new ArrayList<>();

        for (String argument: args) {
            if(argument.startsWith(param)){
                arg.add(argument.substring(param.length()));
            } else if(argument.endsWith(param)) {
                arg.add(argument);
            }
        }

        return arg;
    }

    @NotNull
    public static String cutTo(@NotNull final String str) {
        return cap(str, 10);
    }

    @NotNull
    public static String cap(@NotNull final String str, final int cap) {
        if (str.length() < cap) {
            return String.format("%"+cap+"s", str);
        } else {
            return str.substring(0, cap)+ "_";
        }
    }

    @NotNull
    public static Collection<String> pathProduct(@NotNull final Collection<String> a, @NotNull final Collection<String> b, @NotNull final String join) {
        Collection<String> product = new HashSet<>();

        for(String s : a)
            for(String t : b)
                product.add(s.concat(join).concat(t));

        product.addAll(a);

        return product;
    }

    @NotNull
    public static Collection<String> pathProduct(@NotNull final Collection<String> a, @NotNull final Collection<String> b) {
        return pathProduct(a,b,"/");
    }

}
