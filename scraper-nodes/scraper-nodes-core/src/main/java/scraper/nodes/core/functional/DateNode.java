package scraper.nodes.core.functional;

import scraper.annotations.NotNull;
import scraper.annotations.node.Argument;
import scraper.annotations.node.FlowKey;
import scraper.annotations.node.NodePlugin;
import scraper.api.exceptions.ValidationException;
import scraper.api.flow.FlowMap;
import scraper.api.specification.ScrapeInstance;
import scraper.core.AbstractFunctionalNode;
import scraper.core.Template;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Echoes date in given format. Default is ISO 8601: yyyy-MM-dd'T'HH:mm:ss
 *
 * @author Albert Schimpf
 */
@NodePlugin("1.0.0")
public final class DateNode extends AbstractFunctionalNode {

    /** Format of the date */
    @FlowKey(defaultValue = "\"yyyy-MM-dd'T'HH:mm:ss\"") @Argument @NotNull
    private String dateFormat;

    /** Where to put the date */
    @FlowKey(defaultValue = "\"date\"", output = true) @NotNull
    private Template<String> put = new Template<>(){};

    private @NotNull DateFormat parsedDateFormat;

    @Override
    public void init(@NotNull final ScrapeInstance job) throws ValidationException {
        super.init(job);
        parsedDateFormat = new SimpleDateFormat(dateFormat);
    }

    @Override
    public void modify(@NotNull final FlowMap o) {
        String date = parsedDateFormat.format(new Date());

        put.output(o, date);
    }
}
