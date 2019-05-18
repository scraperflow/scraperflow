package scraper.utils;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class StringUtil {

    /** Removes the extension of a filename. Assumes existence of a '.' character in the name */
    public static String removeExtension(String filename) {
        if(filename != null && filename.contains(".")) return filename.substring(0, filename.lastIndexOf('.'));
        return filename;
    }

    /** Reads the body of a file and concatenates the lines with the newline separator */
    public static String readBody(File data) throws IOException {
        return readBody(data, System.getProperty("line.separator"));
    }

    /** Reads the body of a file and concatenates the lines with chosen separator */
    public static String readBody(File data, String separator) throws IOException {
        StringBuilder body = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(data.getAbsolutePath()))) {
            stream.forEachOrdered(line -> body.append(line).append(separator));
        }
        return body.toString();
    }

    /** Reads the body of a file and returns an ordered list */
    public static List<String> readBodyToList(File data) throws IOException {
        List<String> body = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(data.getAbsolutePath()))) {
            stream.forEachOrdered(body::add);
        }
        return body;
    }

    /** Reads a file and applies an action for each line */
    public static void readBody(File data, Consumer<String> action) throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(data.getAbsolutePath()))) {
            stream.forEachOrdered(action);
        }
    }

    /** Returns the first argument starting with given parameter and delimiter ':' */
    public static String getArgument(String[] args, String param){
        for (String argument: args) {
            if(argument.startsWith(param)){
                String arg =  argument.substring(param.length());
                if (arg.equalsIgnoreCase("null")) return null;
                return arg;
            }
        }

        return null;
    }

    /** Returns all arguments starting with given parameter and delimiter ':' */
    public static List<String> getAllArguments(String[] args, String param){
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

    public static String cutTo(String str) {
        return cap(str, 10);
    }

    public static String cap(String str, int cap) {
        if (str.length() < cap) {
            return String.format("%"+cap+"s", str);
        } else {
            return str.substring(0, cap)+ "_";
        }
    }

    public static Collection<String> pathProduct(Collection<String> a, Collection<String> b, String join) {
        Collection<String> product = new HashSet<>();

        for(String s : a) for(String t : b) product.add(s.concat(join).concat(t));

        product.addAll(a);

        return product;
    }

    public static Collection<String> pathProduct(Collection<String> a, Collection<String> b) {
        return pathProduct(a,b,"/");
    }

}
