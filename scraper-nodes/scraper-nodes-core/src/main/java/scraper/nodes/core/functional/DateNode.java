package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.flow.FlowMap;
import scraper.api.node.container.FunctionalNodeContainer;
import scraper.api.node.container.NodeContainer;
import scraper.api.node.type.FunctionalNode;
import scraper.api.node.type.Node;
import scraper.api.specification.ScrapeInstance;
import scraper.api.template.L;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Echoes date in given format.
 */
@NodePlugin("0.1.0")
public final class DateNode implements FunctionalNode {

    /** Format of the date */
    @FlowKey(defaultValue = "\"yyyy-MM-dd'T'HH:mm:ss\"") @Argument
    private String dateFormat;

    /** Where to put the date */
    @FlowKey(defaultValue = "\"date\"") @NotNull
    private final L<String> put = new L<>(){};

    private DateFormat parsedDateFormat;

    @Override
    public void init(@NotNull NodeContainer<? extends Node> n, @NotNull final ScrapeInstance job) {
        parsedDateFormat = new SimpleDateFormat(dateFormat);
    }

    @Override
    public void modify(@NotNull FunctionalNodeContainer n, @NotNull final FlowMap o) {
        String date = parsedDateFormat.format(new Date());

        o.output(put, date);
    }
}
