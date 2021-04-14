package scraper.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class WC {

    static Map<String, Integer> counts = new HashMap<>();

    static Lock l = new ReentrantLock();
    static boolean par = false;

    public static void main(String[] args) {
        par = Boolean.parseBoolean(args[1]);

        IntStream strm = IntStream.range(0, Integer.parseInt(args[0]));
        if(par) strm = strm.parallel();
        strm.forEach(i -> {
            String file = "bible.txt";
            StringBuilder fileContent = new StringBuilder();
            try {
                Files.readAllLines(Paths.get(file)).forEach(fileContent::append);
                consumeLine(fileContent.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println(counts.get("god"));
    }

    private static void consumeLine(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        Map<String, Integer> localState = new HashMap<>();

        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            localState.merge(token, 1, Integer::sum);
        }

        if(par) {
            l.lock();
            try {
                for (String key : localState.keySet()) {
                    counts.merge(key, localState.get(key), Integer::sum);
                }
            } finally {
                l.unlock();
            }
        } else {
            for (String key : localState.keySet()) {
                counts.merge(key, localState.get(key), Integer::sum);
            }
        }
    }
}
