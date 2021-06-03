package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.Argument;
import scraper.annotations.FlowKey;
import scraper.annotations.NodePlugin;
import scraper.api.FlowMap;
import scraper.api.FunctionalNodeContainer;
import scraper.api.NodeContainer;
import scraper.api.FunctionalNode;
import scraper.api.Node;
import scraper.api.ScrapeInstance;
import scraper.api.L;
import scraper.api.T;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Echoes date in given format.
 */
@NodePlugin("0.3.0")
public final class Date implements FunctionalNode {

    /** Format of the date */
    @FlowKey(defaultValue = "\"yyyy-MM-dd'T'HH:mm:ss\"") @Argument
    private String dateFormat;

    /** Which timestamp for parse in ms if needed */
    @FlowKey
    private final T<Integer> use = new T<>(){};

    /** Where to put the date */
    @FlowKey(mandatory = true)
    private final L<String> put = new L<>(){};

    private DateFormat parsedDateFormat;

    @Override
    public void init(@NotNull NodeContainer<? extends Node> n, @NotNull final ScrapeInstance job) {
        parsedDateFormat = new SimpleDateFormat(dateFormat);
    }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        o.evalMaybe(use).ifPresentOrElse(u -> {
                    String date = parsedDateFormat.format(new java.util.Date((long)u *1000));
                    o.output(put, date);
                }, () -> {
                    String date = parsedDateFormat.format(new java.util.Date());
                    o.output(put, date);
                });
    }
}
