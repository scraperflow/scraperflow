package scraper.plugins;

import scraper.api.di.DIContainer;
import scraper.api.plugin.PreHook;


/**
 * @author Albert Schimpf
 */
public final class NoPreHook implements PreHook {
    @Override
    public void execute(DIContainer dependencies, String[] args) {
        System.setProperty("noprehook", "true");
    }
}
