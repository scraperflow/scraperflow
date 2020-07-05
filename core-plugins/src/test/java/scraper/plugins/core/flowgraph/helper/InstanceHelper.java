package scraper.plugins.core.flowgraph.helper;

import scraper.api.di.DIContainer;
import scraper.api.exceptions.ValidationException;
import scraper.api.plugin.ScrapeSpecificationParser;
import scraper.api.specification.ScrapeSpecification;
import scraper.util.DependencyInjectionUtil;

import java.io.File;
import java.io.IOException;

public class InstanceHelper {
    private static final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    public static ScrapeSpecification getInstance(File scrapeFile) throws IOException, ValidationException {
        ScrapeSpecification spec = null;
        for (ScrapeSpecificationParser p : deps.getCollection(ScrapeSpecificationParser.class)) {
            try {
                spec = p.parseSingle(scrapeFile).get();
            } catch (Exception ignored){}
        }

        return spec;
    }
}
