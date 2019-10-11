package scraper.hooks;

import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;
import scraper.api.plugin.PreHook;

public class TestPreHook implements PreHook {

    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args) throws Exception {
        System.setProperty("test-pre-hook", "true");
    }
}
