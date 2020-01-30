package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.reflect.T;
import scraper.api.specification.ScrapeInstance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Echoes date in given format. Default is ISO 8601: yyyy-MM-dd'T'HH:mm:ss
 *
 * @author Albert Schimpf
 */
@NodePlugin("1.0.0")
public final class DateNode implements FunctionalNode {

    /** Format of the date */
    @FlowKey(defaultValue = "\"yyyy-MM-dd'T'HH:mm:ss\"") @Argument
    private String dateFormat;

    /** Where to put the date */
    @FlowKey(defaultValue = "\"date\"", output = true) @NotNull
    private T<String> put = new T<>(){};

    private DateFormat parsedDateFormat;

    @Override
    public void init(@NotNull NodeContainer n, @NotNull final ScrapeInstance job) {
        parsedDateFormat = new SimpleDateFormat(dateFormat);
    }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String date = parsedDateFormat.format(new Date());

        o.output(put, date);
    }
}
