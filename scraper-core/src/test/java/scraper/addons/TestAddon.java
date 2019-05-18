package scraper.addons;

import scraper.api.di.DIContainer;
import scraper.api.plugin.Addon;

public class TestAddon implements Addon {
    @Override
    public void load(DIContainer loadedDependencies) {
        System.setProperty("test-addon", "true");
    }
}
