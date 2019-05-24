package scraper.plugins;

import scraper.api.di.DIContainer;
import scraper.api.plugin.Hook;
import scraper.api.specification.ScrapeInstance;
import scraper.api.specification.ScrapeSpecification;

import java.util.Map;


/**
 * @author Albert Schimpf
 */
public final class NoPlugin implements Hook {
    @Override
    public void execute(DIContainer dependencies, String[] args, Map<ScrapeSpecification, ScrapeInstance> scraper) {
        System.setProperty("noplugin", "true");
    }
}
