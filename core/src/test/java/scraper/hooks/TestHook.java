package scraper.hooks;

import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;
import scraper.api.plugin.Hook;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;

import java.util.Map;

public class TestHook implements Hook {

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args, @NotNull Map<ScrapeSpecification, ScrapeInstance> scraper) throws Exception {
        System.setProperty("test-hook", "true");
    }
}
