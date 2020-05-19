package scraper.core;

import scraper.api.di.DIContainer;
import scraper.api.exceptions.ValidationException;
import scraper.api.plugin.ScrapeSpecificationParser;
import scraper.api.specification.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.util.DependencyInjectionUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class InstanceHelper {
    private static final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    static ScrapeInstaceImpl getInstance(URL base, String scrapeFile, String... args) throws IOException, ValidationException {
        ScrapeSpecification spec = null;
        for (ScrapeSpecificationParser p : deps.getCollection(ScrapeSpecificationParser.class)) {
            try {
                spec = p.parseSingle(Path.of(base.getFile(), scrapeFile).toFile()).get();
                for (String arg : args) { spec = spec.with(arg); }
            } catch (Exception ignored){}
        }

        return Objects.requireNonNull(deps.get(JobFactory.class)).convertScrapeJob(spec);
    }
}
