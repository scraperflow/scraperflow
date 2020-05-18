package scraper.plugins.core.flowgraph.helper;

import scraper.api.di.DIContainer;
import scraper.api.exceptions.ValidationException;
import scraper.api.plugin.ScrapeSpecificationParser;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.core.JobFactory;
import scraper.util.DependencyInjectionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class InstanceHelper {
    private static final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    public static ScrapeSpecification getInstance(File scrapeFile) throws IOException, ValidationException {
        ScrapeSpecification spec = null;
        for (ScrapeSpecificationParser p : deps.getCollection(ScrapeSpecificationParser.class)) {
            try {
                spec = p.parseSingle(scrapeFile);
            } catch (Exception ignored){}
        }

        return spec;
    }
}
