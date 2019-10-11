package scraper.plugins;

import scraper.annotations.NotNull;
import scraper.api.di.DIContainer;
import scraper.api.plugin.PreHook;


/**
 * @author Albert Schimpf
 */
public final class NoPreHook implements PreHook {
    @Override
    public void execute(@NotNull DIContainer dependencies, @NotNull String[] args) {
        System.setProperty("noprehook", "true");
    }
}
