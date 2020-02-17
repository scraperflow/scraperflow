package scraper.hooks;


import scraper.annotations.ArgsCommand;
import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;
import scraper.api.plugin.Hook;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;
import scraper.utils.StringUtil;

import java.util.Map;

/**
 * A basic hook to provide an exit mechanism after hooks all hooks are executed
 * <p>
 *     Can be used to generate a control flow graph an exit after generation
 */
@ArgsCommand(
        value = "exit",
        doc = "Exits the application after all hooks have been executed",
        example = "scraper app.scrape cf exit"
)
public class ExitHook implements Hook {

    @Override
    public void execute(@NotNull final DIContainer dependencies, @NotNull final String[] args,
                        @NotNull final Map<ScrapeSpecification, ScrapeInstance> jobs) {
        if (StringUtil.getArgument(args, "exit") != null) {
            System.setProperty("scraper.exit", "true");
        }
    }

}
