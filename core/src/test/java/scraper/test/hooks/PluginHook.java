package scraper.test.hooks;

import scraper.annotations.NotNull;
import scraper.api.DIContainer;
import scraper.api.Hook;
import scraper.api.ScrapeInstance;
import scraper.api.ScrapeSpecification;

import java.util.Map;

// Hook coming from another jar
public class PluginHook implements Hook {

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args, @NotNull Map<ScrapeSpecification, ScrapeInstance> scraper) throws Exception {
        System.setProperty("plugin-hook", "true");
    }
}
