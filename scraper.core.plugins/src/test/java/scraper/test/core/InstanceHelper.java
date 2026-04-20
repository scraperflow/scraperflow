package scraper.test.core;

import scraper.api.DIContainer;
import scraper.api.ValidationException;
import scraper.api.ScrapeSpecificationParser;
import scraper.api.ScrapeSpecification;
import scraper.api.specification.impl.ScrapeInstaceImpl;
import scraper.core.JobFactory;
import scraper.util.DependencyInjectionUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

public class InstanceHelper {
    private static final DIContainer deps = DependencyInjectionUtil.getDIContainer();

    static ScrapeInstaceImpl getInstance(URL base, String scrapeFile, String... args) throws IOException, ValidationException {
        ScrapeSpecification spec = null;
        Objects.requireNonNull(base);

        Collection<ScrapeSpecificationParser> parsers = deps.getCollection(ScrapeSpecificationParser.class);
        if(parsers.isEmpty()) throw new IllegalStateException("No parsers found");
        for (ScrapeSpecificationParser p : parsers) {
            try {
                spec = p.parseSingle(Path.of(base.getFile(), scrapeFile).toFile()).get();
                for (String arg : args) { spec = spec.with(arg); }
            } catch (Exception ignored){}
        }


        return Objects.requireNonNull(deps.get(JobFactory.class)).convertScrapeJob(spec);
    }
}
