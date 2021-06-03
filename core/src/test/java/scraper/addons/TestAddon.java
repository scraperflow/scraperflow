package scraper.addons;

import scraper.annotations.NotNull;
import scraper.api.DIContainer;
import scraper.api.Addon;

public class TestAddon implements Addon {
    @Override
    public void load(@NotNull DIContainer loadedDependencies, @NotNull String[] args) {
        System.setProperty("test-addon", "true");
    }
}
