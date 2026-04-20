package scraper.test.providers;

import scraper.annotations.NotNull;
import scraper.api.Addon;
import scraper.api.DIContainer;

public class TestAddon implements Addon {
    @Override
    public void load(@NotNull DIContainer loadedDependencies, @NotNull String[] args) {
        System.setProperty("test-addon", "true");
    }
}
