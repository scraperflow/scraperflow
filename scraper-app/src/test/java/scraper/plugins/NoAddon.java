package scraper.plugins;

import scraper.api.di.DIContainer;
import scraper.api.plugin.Addon;


/**
 * @author Albert Schimpf
 */
public final class NoAddon implements Addon {
    @Override
    public void load(DIContainer loadedDependencies) {
        System.setProperty("noaddon", "true");
    }
}
