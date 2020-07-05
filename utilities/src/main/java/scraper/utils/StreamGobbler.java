package scraper.utils;

import scraper.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class StreamGobbler implements Runnable {
    private @NotNull final InputStream inputStream;
    private @NotNull final Consumer<String> consumeInputLine;

    public StreamGobbler(@NotNull InputStream inputStream, @NotNull Consumer<String> consumeInputLine) {
        this.inputStream = inputStream;
        this.consumeInputLine = consumeInputLine;
    }

    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
    }
}