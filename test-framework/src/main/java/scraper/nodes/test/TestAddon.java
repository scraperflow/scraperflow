package scraper.nodes.test;

import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;
import scraper.api.plugin.Addon;

public class TestAddon implements Addon {
    @Override
    public void load(@NotNull DIContainer loadedDependencies, @NotNull String[] args) {
        System.setProperty("test-addon", "true");
    }
}
